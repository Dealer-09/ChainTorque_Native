package com.example.chaintorquenative

import android.app.Application
import com.reown.android.Core
import com.reown.android.CoreClient
import com.reown.appkit.client.AppKit
import com.reown.appkit.client.Modal
import com.reown.appkit.presets.AppKitChainsPresets
import dagger.hilt.android.HiltAndroidApp
import android.util.Log

private const val TAG = "ChainTorqueApp"

@HiltAndroidApp
class ChainTorqueApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        initWalletConnect()
    }

    private fun initWalletConnect() {
        val appMetaData = Core.Model.AppMetaData(
            name = "ChainTorque",
            description = "3D Model NFT Marketplace",
            url = "https://chaintorque.com",
            icons = listOf("https://chaintorque.com/logo.png"),
            redirect = "chaintorque://wc"
        )

        // Define Sepolia chain explicitly in case preset key differs
        val sepoliaChain = AppKitChainsPresets.ethChains["11155111"] ?: Modal.Model.Chain(
            chainName = "Ethereum Sepolia",
            chainNamespace = "eip155",
            chainReference = "11155111",
            requiredMethods = listOf("eth_sendTransaction", "personal_sign", "eth_signTypedData"),
            optionalMethods = listOf("eth_accounts", "eth_chainId"),
            events = listOf("chainChanged", "accountsChanged"),
            token = Modal.Model.Token(name = "Sepolia ETH", symbol = "ETH", decimal = 18),
            rpcUrl = "https://rpc.sepolia.org",
            blockExplorerUrl = "https://sepolia.etherscan.io"
        )

        // IMPORTANT: Set chains BEFORE initializing so the session namespace uses Sepolia only
        AppKit.setChains(listOf(sepoliaChain))
        Log.d(TAG, "Chains set to Sepolia only: ${sepoliaChain.id}")

        CoreClient.initialize(
            application = this,
            projectId = "b70eaea6d21665a88e8ffea79bd02b2a",
            metaData = appMetaData,
        ) { error ->
            Log.e(TAG, "CoreClient init error: ${error.throwable.message}")
        }

        AppKit.initialize(
            init = Modal.Params.Init(core = CoreClient),
            onSuccess = {
                Log.d(TAG, "AppKit initialized successfully on Sepolia")
            },
            onError = { error ->
                Log.e(TAG, "AppKit init error: ${error.throwable.message}")
            }
        )
    }
}