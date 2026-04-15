package com.example.chaintorquenative.wallet

import android.util.Log
import com.reown.appkit.client.AppKit
import com.reown.appkit.client.Modal
import com.reown.appkit.client.models.request.Request
import com.reown.appkit.client.models.Session
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

private const val TAG = "WalletConnect"
private const val SEPOLIA_CHAIN_ID = "11155111"

/**
 * Singleton that wraps the Reown AppKit (WalletConnect v2) SDK.
 *
 * Replaces the old MetaMaskManager which relied on a broken AIDL service.
 * WalletConnect uses a WebSocket relay so it works on all devices/emulators.
 *
 * Connection flow:
 *  1. UI opens AppKit modal (handled by Compose appKitGraph + openAppKit)
 *  2. User scans QR or deep-links to MetaMask / any WC-compatible wallet
 *  3. Wallet approves session -> onSessionApproved fires -> address verified
 *  4. Purchases use AppKit.request(eth_sendTransaction) via the relay
 */
@Singleton
class WalletConnectManager @Inject constructor() {

    private val _connectionState = MutableStateFlow<WalletConnectionState>(WalletConnectionState.Disconnected)
    val connectionState: StateFlow<WalletConnectionState> = _connectionState.asStateFlow()

    /**
     * AppKit.ModalDelegate that receives all session lifecycle events from the
     * WalletConnect relay. This is set once and stays active for the app lifetime.
     */
    private val modalDelegate = object : AppKit.ModalDelegate {
        override fun onSessionApproved(approvedSession: Modal.Model.ApprovedSession) {
            Log.d(TAG, "Session approved")

            // ApprovedSession is a sealed class with WalletConnectSession and CoinbaseSession subtypes
            when (approvedSession) {
                is Modal.Model.ApprovedSession.WalletConnectSession -> {
                    val account = approvedSession.accounts.firstOrNull()
                    if (account != null) {
                        // Account format is "eip155:chainId:address"
                        val addr = account.substringAfterLast(":")
                        val chainPart = account.substringAfter(":").substringBefore(":")
                        Log.d(TAG, "Connected via WalletConnect: addr=$addr chain=$chainPart")
                        _connectionState.value = WalletConnectionState.Connected(
                            address = addr,
                            chainId = chainPart.ifBlank { SEPOLIA_CHAIN_ID }
                        )
                    }
                }
                is Modal.Model.ApprovedSession.CoinbaseSession -> {
                    val addr = approvedSession.address
                    Log.d(TAG, "Connected via Coinbase: addr=$addr")
                    _connectionState.value = WalletConnectionState.Connected(
                        address = addr,
                        chainId = approvedSession.networkId.ifBlank { SEPOLIA_CHAIN_ID }
                    )
                }
            }
        }

        override fun onSessionRejected(rejectedSession: Modal.Model.RejectedSession) {
            Log.w(TAG, "Session rejected: ${rejectedSession.reason}")
            _connectionState.value = WalletConnectionState.Error(
                "Wallet rejected the connection: ${rejectedSession.reason}"
            )
        }

        override fun onSessionUpdate(updatedSession: Modal.Model.UpdatedSession) {
            Log.d(TAG, "Session updated")
        }

        override fun onSessionExtend(session: Modal.Model.Session) {
            Log.d(TAG, "Session extended")
        }

        override fun onSessionEvent(sessionEvent: Modal.Model.SessionEvent) {
            Log.d(TAG, "Session event: ${sessionEvent.name}")
        }

        override fun onSessionDelete(deletedSession: Modal.Model.DeletedSession) {
            when (deletedSession) {
                is Modal.Model.DeletedSession.Success -> {
                    Log.d(TAG, "Session deleted: ${deletedSession.reason}")
                }
                is Modal.Model.DeletedSession.Error -> {
                    Log.w(TAG, "Session delete error: ${deletedSession.error.message}")
                }
            }
            _connectionState.value = WalletConnectionState.Disconnected
        }

        override fun onSessionRequestResponse(response: Modal.Model.SessionRequestResponse) {
            Log.d(TAG, "Session request response received")
            handleRequestResponse(response)
        }

        override fun onProposalExpired(proposal: Modal.Model.ExpiredProposal) {
            Log.w(TAG, "Proposal expired")
        }

        override fun onRequestExpired(request: Modal.Model.ExpiredRequest) {
            Log.w(TAG, "Request expired")
            pendingTxCallback?.invoke(false, null, "Transaction request expired")
            pendingTxCallback = null
        }

        override fun onConnectionStateChange(state: Modal.Model.ConnectionState) {
            Log.d(TAG, "Connection state changed: isAvailable=${state.isAvailable}")
        }

        override fun onError(error: Modal.Model.Error) {
            Log.e(TAG, "AppKit error: ${error.throwable.message}")
            val current = _connectionState.value
            if (current is WalletConnectionState.Connecting) {
                _connectionState.value = WalletConnectionState.Error(
                    error.throwable.message ?: "Connection error"
                )
            }
        }
    }

    // Transaction callback management
    private var pendingTxCallback: ((success: Boolean, txHash: String?, error: String?) -> Unit)? = null

    init {
        AppKit.setDelegate(modalDelegate)
        Log.d(TAG, "WalletConnectManager initialized, delegate set")

        // Check if there is an existing session
        checkExistingSession()
    }

    /**
     * Check if AppKit already has an active session from a previous app launch.
     */
    private fun checkExistingSession() {
        try {
            val session = AppKit.getSession()
            if (session != null) {
                when (session) {
                    is Session.WalletConnectSession -> {
                        // Verify the session namespace includes Sepolia
                        val eip155Ns = session.namespaces["eip155"]
                        val sessionChains = eip155Ns?.chains ?: emptyList()
                        val sessionAccounts = eip155Ns?.accounts ?: emptyList()
                        val hasSepolia = sessionChains.any { it.contains(SEPOLIA_CHAIN_ID) }
                            || sessionAccounts.any { it.contains(SEPOLIA_CHAIN_ID) }

                        Log.d(TAG, "WC session chains=$sessionChains accounts=$sessionAccounts hasSepolia=$hasSepolia")

                        if (!hasSepolia) {
                            Log.w(TAG, "Session missing Sepolia, disconnecting...")
                            forceDisconnect()
                            return
                        }

                        val account = AppKit.getAccount()
                        if (account != null) {
                            Log.d(TAG, "Restored Sepolia session: ${account.address}")
                            _connectionState.value = WalletConnectionState.Connected(
                                address = account.address,
                                chainId = SEPOLIA_CHAIN_ID
                            )
                        }
                    }
                    is Session.CoinbaseSession -> {
                        val account = AppKit.getAccount()
                        if (account != null) {
                            _connectionState.value = WalletConnectionState.Connected(
                                address = account.address,
                                chainId = SEPOLIA_CHAIN_ID
                            )
                        }
                    }
                }
            } else {
                Log.d(TAG, "No existing session found")
            }
        } catch (e: Exception) {
            Log.w(TAG, "Error checking existing session: ${e.message}")
        }
    }

    private fun forceDisconnect() {
        AppKit.disconnect(
            onSuccess = {
                Log.d(TAG, "Stale session cleared successfully")
                _connectionState.value = WalletConnectionState.Disconnected
            },
            onError = { throwable: Throwable ->
                Log.w(TAG, "Stale session clear error: ${throwable.message}")
                _connectionState.value = WalletConnectionState.Disconnected
            }
        )
    }

    /**
     * Called by viewmodel before opening the AppKit modal.
     * The actual connection is triggered by the AppKit modal UI (QR code / deep link).
     */
    fun prepareConnect() {
        _connectionState.value = WalletConnectionState.Connecting
    }

    /**
     * Disconnect the current wallet session.
     */
    fun disconnect() {
        AppKit.disconnect(
            onSuccess = {
                Log.d(TAG, "Disconnected successfully")
                _connectionState.value = WalletConnectionState.Disconnected
            },
            onError = { throwable: Throwable ->
                Log.e(TAG, "Disconnect error: ${throwable.message}")
                // Force local state to disconnected even if the relay fails
                _connectionState.value = WalletConnectionState.Disconnected
            }
        )
    }

    /** Returns the currently connected wallet address, or null. Only used internally. */
    internal fun getConnectedAddress(): String? = try { AppKit.getAccount()?.address } catch (e: Exception) { null }

    /**
     * Send an eth_sendTransaction request via the WalletConnect relay.
     * The wallet app (MetaMask, etc.) will open for the user to approve.
     */
    fun sendTransaction(
        fromAddress: String,
        toAddress: String,
        data: String,
        value: String = "0x0",
        onSuccess: (String) -> Unit,
        onError: (String) -> Unit
    ) {
        pendingTxCallback = { success, txHash, errorMsg ->
            if (success && txHash != null) {
                onSuccess(txHash)
            } else {
                onError(errorMsg ?: "Transaction failed")
            }
        }

        // JSON-RPC params for eth_sendTransaction
        val txParams = "[{\"from\":\"$fromAddress\",\"to\":\"$toAddress\",\"data\":\"$data\",\"value\":\"$value\"}]"

        // Use the new Request model (not deprecated Modal.Params.Request)
        val request = Request(
            method = "eth_sendTransaction",
            params = txParams,
        )

        val onSuccessCallback: () -> Unit = {
            Log.d(TAG, "Transaction request sent successfully, awaiting wallet response")
            // The actual result comes through onSessionRequestResponse
        }
        AppKit.request(
            request = request,
            onSuccess = onSuccessCallback,
            onError = { throwable: Throwable ->
                Log.e(TAG, "Transaction request error: ${throwable.message}")
                pendingTxCallback?.invoke(false, null, throwable.message ?: "Request failed")
                pendingTxCallback = null
            }
        )
    }

    /**
     * Handle the response when wallet answers a request (e.g., tx hash).
     */
    private fun handleRequestResponse(response: Modal.Model.SessionRequestResponse) {
        when (val result = response.result) {
            is Modal.Model.JsonRpcResponse.JsonRpcResult -> {
                val txHash = result.result?.toString()
                Log.d(TAG, "Transaction successful: $txHash")
                pendingTxCallback?.invoke(true, txHash, null)
                pendingTxCallback = null
            }
            is Modal.Model.JsonRpcResponse.JsonRpcError -> {
                Log.e(TAG, "Transaction error: ${result.message}")
                pendingTxCallback?.invoke(false, null, result.message)
                pendingTxCallback = null
            }
        }
    }
}