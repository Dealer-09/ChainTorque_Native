// API Service for ChainTorque Mobile App (Kotlin/Android)
// This mirrors the web app's apiService.ts but in Kotlin

package com.example.chaintorquenative.mobile.data.api

import retrofit2.Response
import retrofit2.http.*

// Data Classes (Models)
data class ApiResponse<T>(
    val success: Boolean,
    val data: T?,
    val message: String?,
    val error: String?
)

data class MarketplaceItem(
    val tokenId: String,
    val title: String,
    val description: String,
    val price: Double,
    val priceETH: Double?,
    val seller: Seller,
    val images: List<String>,
    val modelUrl: String,
    val category: String,
    val tags: List<String>,
    val views: Int,
    val likes: Int,
    val name: String,
    val createdAt: String,
    val blockchain: String?,
    val format: String?
)

data class Seller(
    val name: String,
    val avatar: String,
    val verified: Boolean,
    val rating: Double,
    val totalSales: Int
)

data class UserNFT(
    val tokenId: Int,
    val title: String,
    val description: String,
    val image: String,
    val modelUrl: String?,
    val owner: String,
    val createdAt: String
)

data class UserProfile(
    val address: String,
    val name: String?,
    val avatar: String?,
    val verified: Boolean,
    val totalNFTs: Int,
    val totalSales: Int,
    val memberSince: String
)

data class Web3Status(
    val connected: Boolean,
    val account: String?,
    val network: String?,
    val balance: String?
)

data class PurchaseRequest(
    val tokenId: Int,
    val buyerAddress: String,
    val price: Double
)

data class PurchaseResponse(
    val transactionHash: String
)

// Retrofit API Interface
interface ChainTorqueApiService {

    // Health Check
    @GET("health")
    suspend fun healthCheck(): Response<Map<String, String>>

    // Marketplace Endpoints
    @GET("api/marketplace")
    suspend fun getMarketplaceItems(): Response<ApiResponse<List<MarketplaceItem>>>

    @GET("api/marketplace/{tokenId}")
    suspend fun getMarketplaceItem(@Path("tokenId") tokenId: Int): Response<ApiResponse<MarketplaceItem>>

    @GET("api/marketplace/stats")
    suspend fun getMarketplaceStats(): Response<ApiResponse<Any>>

    // User Endpoints
    @GET("api/user/{address}/nfts")
    suspend fun getUserNFTs(@Path("address") address: String): Response<ApiResponse<List<UserNFT>>>

    @GET("api/user/{address}/purchases")
    suspend fun getUserPurchases(@Path("address") address: String): Response<ApiResponse<List<MarketplaceItem>>>

    @GET("api/user/{address}/sales")
    suspend fun getUserSales(@Path("address") address: String): Response<ApiResponse<List<MarketplaceItem>>>

    @GET("api/user/{address}/profile")
    suspend fun getUserProfile(@Path("address") address: String): Response<ApiResponse<UserProfile>>

    // Purchase Endpoint
    @POST("api/marketplace/purchase")
    suspend fun purchaseNFT(@Body request: PurchaseRequest): Response<ApiResponse<PurchaseResponse>>

    // Web3 Endpoints
    @GET("api/web3/status")
    suspend fun getWeb3Status(): Response<ApiResponse<Web3Status>>

    @POST("api/web3/validate-address")
    suspend fun validateAddress(@Body request: Map<String, String>): Response<ApiResponse<Map<String, Boolean>>>

    @GET("api/web3/balance/{address}")
    suspend fun getBalance(@Path("address") address: String): Response<ApiResponse<Map<String, String>>>
}

/*
Usage Example in Repository:

class MarketplaceRepository @Inject constructor(
    private val apiService: ChainTorqueApiService
) {
    suspend fun getMarketplaceItems(): Result<List<MarketplaceItem>> {
        return try {
            val response = apiService.getMarketplaceItems()
            if (response.isSuccessful && response.body()?.success == true) {
                Result.success(response.body()?.data ?: emptyList())
            } else {
                Result.failure(Exception(response.body()?.error ?: "Unknown error"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun purchaseItem(tokenId: Int, buyerAddress: String, price: Double): Result<String> {
        return try {
            val request = PurchaseRequest(tokenId, buyerAddress, price)
            val response = apiService.purchaseNFT(request)
            if (response.isSuccessful && response.body()?.success == true) {
                Result.success(response.body()?.data?.transactionHash ?: "")
            } else {
                Result.failure(Exception(response.body()?.error ?: "Purchase failed"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
*/
