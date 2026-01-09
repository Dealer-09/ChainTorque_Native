package com.example.chaintorquenative.wallet

import android.app.Application
import android.content.Context
import android.util.Log
import com.reown.android.Core
import com.reown.android.CoreClient
import com.reown.android.relay.ConnectionType
import com.reown.appkit.client.AppKit
import com.reown.appkit.client.Modal
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

private const val TAG = "WalletConnect"

/**
 * WalletConnect configuration for ChainTorque
 */
object WalletConnectConfig {
    const val PROJECT_ID = "b70eaea6d21665a88e8ffea79bd02b2a"
    const val APP_NAME = "ChainTorque"
    const val APP_DESCRIPTION = "Premium 3D CAD Marketplace"
    const val APP_URL = "https://chaintorque.com"
    const val APP_ICON = "https://chaintorque.com/logo.png"
    const val REDIRECT_URL = "chaintorque://walletconnect"
}

/**
 * Wallet connection states
 */
sealed class WalletConnectionState {
    object Disconnected : WalletConnectionState()
    object Connecting : WalletConnectionState()
    data class Connected(
        val address: String,
        val chainId: String,
        val balance: String = "0.0"
    ) : WalletConnectionState()
    data class Error(val message: String) : WalletConnectionState()
}

/**
 * Manages WalletConnect sessions using Reown AppKit
 */
@Singleton
class WalletConnectManager @Inject constructor() {

    private val _connectionState = MutableStateFlow<WalletConnectionState>(WalletConnectionState.Disconnected)
    val connectionState: StateFlow<WalletConnectionState> = _connectionState.asStateFlow()

    private var isInitialized = false
    private var appContext: Context? = null

    /**
     * Initialize AppKit - call this in Application.onCreate()
     */
    fun initialize(application: Application) {
        if (isInitialized) {
            Log.d(TAG, "Already initialized, skipping")
            return
        }
        appContext = application.applicationContext
        Log.d(TAG, "Starting WalletConnect initialization...")

        try {
            val projectId = WalletConnectConfig.PROJECT_ID
            Log.d(TAG, "Using Project ID: $projectId")

            val appMetaData = Core.Model.AppMetaData(
                name = WalletConnectConfig.APP_NAME,
                description = WalletConnectConfig.APP_DESCRIPTION,
                url = WalletConnectConfig.APP_URL,
                icons = listOf(WalletConnectConfig.APP_ICON),
                redirect = WalletConnectConfig.REDIRECT_URL
            )

            Log.d(TAG, "Initializing CoreClient...")
            // Initialize CoreClient
            CoreClient.initialize(
                projectId = projectId,
                connectionType = ConnectionType.AUTOMATIC,
                application = application,
                metaData = appMetaData
            ) { error ->
                Log.e(TAG, "CoreClient error: ${error.throwable.message}", error.throwable)
                _connectionState.value = WalletConnectionState.Error(
                    error.throwable.message ?: "Core initialization failed"
                )
            }

            Log.d(TAG, "CoreClient initialized, now initializing AppKit...")

            // Initialize AppKit
            AppKit.initialize(
                init = Modal.Params.Init(CoreClient),
                onSuccess = {
                    Log.d(TAG, "AppKit initialized successfully!")

                    // Use preset chains from SDK - Sepolia is eip155:11155111
                    // Import: com.reown.appkit.presets.AppKitChainsPresets
                    try {
                        // Try using preset chains first
                        val chains = com.reown.appkit.presets.AppKitChainsPresets.ethChains.values.toList()
                        AppKit.setChains(chains)
                        Log.d(TAG, "Chains configured from presets: ${chains.size} chains")
                    } catch (e: Exception) {
                        Log.w(TAG, "Preset chains not available, skipping setChains: ${e.message}")
                    }

                    isInitialized = true
                    setupDelegate()
                    checkExistingSession()
                },
                onError = { error ->
                    Log.e(TAG, "AppKit error: ${error.throwable.message}", error.throwable)
                    _connectionState.value = WalletConnectionState.Error(
                        error.throwable.message ?: "AppKit initialization failed"
                    )
                }
            )

        } catch (e: Exception) {
            Log.e(TAG, "Initialization exception: ${e.message}", e)
            _connectionState.value = WalletConnectionState.Error(
                e.message ?: "Initialization failed"
            )
        }
    }

    /**
     * Check for existing session on app start
     */
    fun checkExistingSession() {
        try {
            val account = AppKit.getAccount()
            if (account != null) {
                Log.d(TAG, "Found existing session: ${account.address}")
                _connectionState.value = WalletConnectionState.Connected(
                    address = account.address.toString(),
                    chainId = account.chain?.toString() ?: "eip155:11155111"
                )
            } else {
                Log.d(TAG, "No existing session found")
            }
        } catch (e: Exception) {
            Log.d(TAG, "No existing session: ${e.message}")
        }
    }

    /**
     * Set up AppKit delegate for session events
     */
    private fun setupDelegate() {
        Log.d(TAG, "Setting up AppKit delegate...")
        AppKit.setDelegate(object : AppKit.ModalDelegate {
            override fun onSessionApproved(approvedSession: Modal.Model.ApprovedSession) {
                Log.d(TAG, "Session approved!")
                try {
                    val account = AppKit.getAccount()
                    if (account != null) {
                        Log.d(TAG, "Connected address: ${account.address}")
                        _connectionState.value = WalletConnectionState.Connected(
                            address = account.address.toString(),
                            chainId = account.chain?.toString() ?: "eip155:11155111"
                        )
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to get account: ${e.message}", e)
                    _connectionState.value = WalletConnectionState.Error("Failed to get account")
                }
            }

            override fun onSessionRejected(rejectedSession: Modal.Model.RejectedSession) {
                Log.w(TAG, "Session rejected by wallet")
                _connectionState.value = WalletConnectionState.Error("Connection rejected by wallet")
            }

            override fun onSessionUpdate(updatedSession: Modal.Model.UpdatedSession) {
                Log.d(TAG, "Session updated")
            }

            override fun onSessionDelete(deletedSession: Modal.Model.DeletedSession) {
                Log.d(TAG, "Session deleted")
                _connectionState.value = WalletConnectionState.Disconnected
            }

            override fun onSessionEvent(sessionEvent: Modal.Model.SessionEvent) {
                Log.d(TAG, "Session event: $sessionEvent")
            }

            override fun onSessionExtend(session: Modal.Model.Session) {
                Log.d(TAG, "Session extended")
            }

            override fun onSessionRequestResponse(response: Modal.Model.SessionRequestResponse) {
                Log.d(TAG, "Session request response")
            }

            override fun onProposalExpired(proposal: Modal.Model.ExpiredProposal) {
                Log.w(TAG, "Proposal expired")
                _connectionState.value = WalletConnectionState.Error("Connection request expired")
            }

            override fun onRequestExpired(request: Modal.Model.ExpiredRequest) {
                Log.w(TAG, "Request expired")
            }

            override fun onConnectionStateChange(state: Modal.Model.ConnectionState) {
                Log.d(TAG, "Connection state changed: ${state.isAvailable}")
            }

            override fun onError(error: Modal.Model.Error) {
                Log.e(TAG, "AppKit error: ${error.throwable.message}", error.throwable)
                _connectionState.value = WalletConnectionState.Error(
                    error.throwable.message ?: "Unknown error occurred"
                )
            }
        })
    }

    /**
     * Open wallet connection - uses manual pairing with SDK 1.3.3
     */
    fun connect() {
        Log.d(TAG, "connect() called, isInitialized=$isInitialized")

        if (!isInitialized) {
            Log.e(TAG, "AppKit not initialized!")
            _connectionState.value = WalletConnectionState.Error("AppKit not initialized. Please restart the app.")
            return
        }

        _connectionState.value = WalletConnectionState.Connecting

        try {
            Log.d(TAG, "Starting connection...")

            // Create a pairing first
            val pairing = CoreClient.Pairing.create { error ->
                Log.e(TAG, "Pairing creation error: ${error.throwable.message}", error.throwable)
            }

            if (pairing == null) {
                Log.e(TAG, "Failed to create pairing")
                _connectionState.value = WalletConnectionState.Error("Failed to create connection")
                return
            }

            Log.d(TAG, "Pairing created: ${pairing.uri}")

            // SDK 1.3.3 uses AppKit.connect() with namespace params and pairing
            // Use optional namespaces to allow any chain - don't force Sepolia
            val requiredNamespaces = mapOf(
                "eip155" to Modal.Model.Namespace.Proposal(
                    chains = listOf("eip155:1"), // Ethereum Mainnet as minimum requirement
                    methods = listOf(
                        "eth_sendTransaction",
                        "eth_signTransaction",
                        "eth_sign",
                        "personal_sign",
                        "eth_signTypedData"
                    ),
                    events = listOf("chainChanged", "accountsChanged")
                )
            )
            
            val optionalNamespaces = mapOf(
                "eip155" to Modal.Model.Namespace.Proposal(
                    chains = listOf("eip155:11155111", "eip155:1", "eip155:137", "eip155:42161"), // Sepolia, Mainnet, Polygon, Arbitrum
                    methods = listOf(
                        "eth_sendTransaction",
                        "eth_signTransaction",
                        "eth_sign",
                        "personal_sign",
                        "eth_signTypedData",
                        "wallet_switchEthereumChain"
                    ),
                    events = listOf("chainChanged", "accountsChanged")
                )
            )

            AppKit.connect(
                connect = Modal.Params.Connect(
                    namespaces = requiredNamespaces,
                    optionalNamespaces = optionalNamespaces,
                    properties = null,
                    pairing = pairing
                ),
                onSuccess = {
                    Log.d(TAG, "Connection initiated successfully")
                    // Launch wallet app with the pairing URI
                    launchWalletWithUri(pairing.uri)
                },
                onError = { error ->
                    Log.e(TAG, "Connection error: ${error.throwable.message}", error.throwable)
                    _connectionState.value = WalletConnectionState.Error(
                        error.throwable.message ?: "Connection failed"
                    )
                }
            )
        } catch (e: Exception) {
            Log.e(TAG, "connect() exception: ${e.message}", e)
            _connectionState.value = WalletConnectionState.Error(
                "Connection error: ${e.message}"
            )
        }
    }

    /**
     * Launch wallet app with the WalletConnect URI
     */
    private fun launchWalletWithUri(pairingUri: String) {
        Log.d(TAG, "launchWalletWithUri: $pairingUri")

        try {
            val context = appContext
            if (context == null) {
                Log.e(TAG, "Context is null!")
                _connectionState.value = WalletConnectionState.Error("App context not available")
                return
            }

            // Try to launch MetaMask directly
            val metamaskUri = "metamask://wc?uri=${android.net.Uri.encode(pairingUri)}"
            Log.d(TAG, "Launching MetaMask with URI: $metamaskUri")

            val intent = android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse(metamaskUri))
            intent.addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)

            try {
                context.startActivity(intent)
                Log.d(TAG, "MetaMask launched successfully")
            } catch (e: Exception) {
                Log.w(TAG, "MetaMask not found, trying WalletConnect: ${e.message}")

                // MetaMask not installed, try generic WalletConnect deeplink
                val wcUri = "wc:${pairingUri.removePrefix("wc:")}"
                Log.d(TAG, "Trying WC URI: $wcUri")

                val wcIntent = android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse(wcUri))
                wcIntent.addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)

                try {
                    context.startActivity(wcIntent)
                    Log.d(TAG, "WalletConnect launched")
                } catch (e2: Exception) {
                    Log.e(TAG, "No wallet app found: ${e2.message}")
                    _connectionState.value = WalletConnectionState.Error(
                        "No compatible wallet found. Please install MetaMask."
                    )
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "launchWalletWithUri exception: ${e.message}", e)
            _connectionState.value = WalletConnectionState.Error(
                "Failed to launch wallet: ${e.message}"
            )
        }
    }

    /**
     * Disconnect current session
     */
    fun disconnect() {
        Log.d(TAG, "disconnect() called")
        try {
            AppKit.disconnect(
                onSuccess = {
                    Log.d(TAG, "Disconnect successful")
                    _connectionState.value = WalletConnectionState.Disconnected
                },
                onError = { error ->
                    Log.w(TAG, "Disconnect error: $error")
                    _connectionState.value = WalletConnectionState.Disconnected
                }
            )
        } catch (e: Exception) {
            Log.e(TAG, "disconnect() exception: ${e.message}", e)
            _connectionState.value = WalletConnectionState.Disconnected
        }
    }

    /**
     * Get current connected address
     */
    fun getConnectedAddress(): String? {
        return when (val state = _connectionState.value) {
            is WalletConnectionState.Connected -> state.address
            else -> null
        }
    }
}