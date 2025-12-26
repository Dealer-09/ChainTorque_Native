package com.example.chaintorquenative

import android.app.Application
import com.example.chaintorquenative.wallet.WalletConnectManager
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class ChainTorqueApplication : Application() {
    
    @Inject
    lateinit var walletConnectManager: WalletConnectManager
    
    override fun onCreate() {
        super.onCreate()
        
        // Initialize WalletConnect/Reown AppKit
        walletConnectManager.initialize(this)
    }
}
