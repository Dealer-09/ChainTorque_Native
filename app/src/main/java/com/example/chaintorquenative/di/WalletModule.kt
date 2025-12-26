package com.example.chaintorquenative.di

import com.example.chaintorquenative.wallet.WalletConnectManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object WalletModule {

    @Provides
    @Singleton
    fun provideWalletConnectManager(): WalletConnectManager {
        return WalletConnectManager()
    }
}
