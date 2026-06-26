package com.example.chaintorquenative.di

import com.example.chaintorquenative.BuildConfig
import com.example.chaintorquenative.mobile.data.api.ChainTorqueApiService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.IOException
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    // Primary backend URL (read from local.properties/BuildConfig, defaults to production)
    private val PRIMARY_URL = BuildConfig.API_BASE_URL
    // Fallback backend URL (new Render account)
    private const val FALLBACK_URL = "https://chain-torque-backend.onrender.com/"
    
    // Currently active URL (can switch to fallback if primary fails)
    @Volatile
    private var activeBaseUrl: String = PRIMARY_URL

    // Timestamp of when we switched to fallback — allows periodic recovery check
    @Volatile
    private var fallbackSwitchedAtMs: Long = 0L

    // Try primary again every 5 minutes while on fallback
    private const val RECOVERY_INTERVAL_MS = 5 * 60 * 1000L

    /**
     * Interceptor that handles automatic fallback to secondary backend
     * if the primary backend is unavailable (connection errors, 5xx errors).
     * Periodically re-checks the primary URL so we recover once it's back up.
     */
    class FallbackInterceptor : Interceptor {
        override fun intercept(chain: Interceptor.Chain): Response {
            val originalRequest = chain.request()
            val originalUrl = originalRequest.url.toString()

            // If on fallback long enough, probe primary again
            if (activeBaseUrl == FALLBACK_URL &&
                System.currentTimeMillis() - fallbackSwitchedAtMs > RECOVERY_INTERVAL_MS
            ) {
                activeBaseUrl = PRIMARY_URL
                android.util.Log.d("NetworkModule", "🔄 Recovery: retrying primary $activeBaseUrl")
            }

            // Rewrite URL to activeBaseUrl if needed
            val requestToExecute = if (activeBaseUrl == FALLBACK_URL && originalUrl.contains(PRIMARY_URL)) {
                val newUrl = originalUrl.replace(PRIMARY_URL, FALLBACK_URL)
                originalRequest.newBuilder().url(newUrl).build()
            } else {
                originalRequest
            }
            
            return try {
                val response = chain.proceed(requestToExecute)
                
                // If we get a server error (5xx) on primary, try fallback
                if (response.code >= 500 && activeBaseUrl == PRIMARY_URL) {
                    response.close()
                    switchToFallback("5xx response")
                    
                    val newUrl = requestToExecute.url.toString().replace(PRIMARY_URL, FALLBACK_URL)
                    val fallbackRequest = requestToExecute.newBuilder()
                        .url(newUrl)
                        .build()
                    
                    chain.proceed(fallbackRequest)
                } else {
                    response
                }
            } catch (e: IOException) {
                // Connection error on primary, try fallback
                if (activeBaseUrl == PRIMARY_URL) {
                    switchToFallback("IOException: ${e.message}")
                    
                    val newUrl = requestToExecute.url.toString().replace(PRIMARY_URL, FALLBACK_URL)
                    val fallbackRequest = requestToExecute.newBuilder()
                        .url(newUrl)
                        .build()
                    
                    chain.proceed(fallbackRequest)
                } else {
                    throw e
                }
            }
        }

        private fun switchToFallback(reason: String) {
            activeBaseUrl = FALLBACK_URL
            fallbackSwitchedAtMs = System.currentTimeMillis()
            android.util.Log.d("NetworkModule", "🔄 Switching to fallback ($reason): $FALLBACK_URL")
        }
    }

    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = if (BuildConfig.DEBUG) {
                HttpLoggingInterceptor.Level.BODY
            } else {
                HttpLoggingInterceptor.Level.BASIC
            }
        }
        
        return OkHttpClient.Builder()
            .addInterceptor(FallbackInterceptor())
            .addInterceptor(loggingInterceptor)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    @Provides
    @Singleton
    fun provideRetrofit(okHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl(PRIMARY_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    @Provides
    @Singleton
    fun provideApiService(retrofit: Retrofit): ChainTorqueApiService {
        return retrofit.create(ChainTorqueApiService::class.java)
    }
}
