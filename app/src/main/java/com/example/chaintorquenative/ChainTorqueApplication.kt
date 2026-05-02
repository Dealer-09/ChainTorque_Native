package com.example.chaintorquenative

import android.app.Application
import com.reown.android.Core
import com.reown.android.CoreClient
import com.reown.appkit.client.AppKit
import com.reown.appkit.client.Modal
import com.reown.appkit.utils.EthUtils
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

        // Sepolia testnet chain — uses same EthUtils methods as all preset chains
        val sepoliaChain = Modal.Model.Chain(
            chainName = "Ethereum Sepolia",
            chainNamespace = "eip155",
            chainReference = "11155111",
            requiredMethods = EthUtils.ethRequiredMethods,
            optionalMethods = EthUtils.ethOptionalMethods,
            events = EthUtils.ethEvents,
            token = Modal.Model.Token(name = "Sepolia ETH", symbol = "ETH", decimal = 18),
            rpcUrl = BuildConfig.RPC_URL,
            blockExplorerUrl = "https://sepolia.etherscan.io"
        )

        // Set Sepolia as the ONLY chain before initialization
        AppKit.setChains(listOf(sepoliaChain))
        Log.d(TAG, "Chains set to Sepolia only: ${sepoliaChain.id}")

        CoreClient.initialize(
            application = this,
            projectId = BuildConfig.WALLETCONNECT_PROJECT_ID,
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