package com.example.chaintorquenative.wallet

import android.app.Application
import android.content.Context
import android.content.Intent
import android.net.Uri
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
            val connectionType = ConnectionType.AUTOMATIC
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
                connectionType = connectionType,
                application = application,
                metaData = appMetaData
            ) { error ->
                Log.e(TAG, "CoreClient initialization error: ${error.throwable.message}", error.throwable)
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
                    isInitialized = true
                    setupDelegate()
                    checkExistingSession()
                },
                onError = { error ->
                    Log.e(TAG, "AppKit initialization error: ${error.throwable.message}", error.throwable)
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
    private fun checkExistingSession() {
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
     * Open wallet connection - creates pairing and launches wallet
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
            Log.d(TAG, "Creating pairing...")

            // Create a new pairing and get the URI
            val pairing = CoreClient.Pairing.create { error ->
                Log.e(TAG, "Pairing creation error: ${error.throwable.message}", error.throwable)
                _connectionState.value = WalletConnectionState.Error(
                    error.throwable.message ?: "Failed to create pairing"
                )
            }

            if (pairing != null) {
                val pairingUri = pairing.uri
                Log.d(TAG, "Pairing created with URI: $pairingUri")

                // Connect using the pairing
                Log.d(TAG, "Calling AppKit.connect()...")
                AppKit.connect(
                    connect = Modal.Params.Connect(
                        namespaces = mapOf(
                            "eip155" to Modal.Model.Namespace.Proposal(
                                chains = listOf("eip155:11155111"), // Sepolia
                                methods = listOf(
                                    "eth_sendTransaction",
                                    "eth_signTransaction",
                                    "eth_sign",
                                    "personal_sign",
                                    "eth_signTypedData"
                                ),
                                events = listOf("chainChanged", "accountsChanged")
                            )
                        ),
                        optionalNamespaces = null,
                        properties = null,
                        pairing = pairing
                    ),
                    onSuccess = {
                        Log.d(TAG, "AppKit.connect() onSuccess - launching wallet...")
                        launchWalletWithUri(pairingUri)
                    },
                    onError = { error ->
                        Log.e(TAG, "AppKit.connect() error: ${error.throwable.message}", error.throwable)
                        _connectionState.value = WalletConnectionState.Error(
                            error.throwable.message ?: "Connection failed"
                        )
                    }
                )
            } else {
                Log.e(TAG, "Pairing is null!")
                _connectionState.value = WalletConnectionState.Error("Failed to create connection")
            }
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
            val metamaskUri = "metamask://wc?uri=${Uri.encode(pairingUri)}"
            Log.d(TAG, "Launching MetaMask with URI: $metamaskUri")

            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(metamaskUri))
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

            try {
                context.startActivity(intent)
                Log.d(TAG, "MetaMask launched successfully")
            } catch (e: Exception) {
                Log.w(TAG, "MetaMask not found, trying WalletConnect: ${e.message}")

                // MetaMask not installed, try generic WalletConnect deeplink
                val wcUri = "wc:${pairingUri.removePrefix("wc:")}"
                Log.d(TAG, "Trying WC URI: $wcUri")

                val wcIntent = Intent(Intent.ACTION_VIEW, Uri.parse(wcUri))
                wcIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

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