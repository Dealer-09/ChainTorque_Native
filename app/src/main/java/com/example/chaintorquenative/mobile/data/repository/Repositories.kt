package com.example.chaintorquenative.mobile.data.repository

import android.util.Log
import com.example.chaintorquenative.mobile.data.api.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.math.BigDecimal
import java.math.BigInteger
import java.math.RoundingMode
import javax.inject.Inject
import javax.inject.Singleton

// ─── Marketplace Repository ───────────────────────────────────────────────────

@Singleton
class MarketplaceRepository @Inject constructor(
    private val apiService: ChainTorqueApiService
) {
    suspend fun getMarketplaceItems(): Result<List<MarketplaceItem>> = runCatching {
        val r = apiService.getMarketplaceItems()
        r.body()?.takeIf { r.isSuccessful && it.success }?.data
            ?: throw Exception(r.body()?.error ?: "API ${r.code()}")
    }

    suspend fun getMarketplaceItem(tokenId: Int): Result<MarketplaceItem> = runCatching {
        val r = apiService.getMarketplaceItem(tokenId)
        r.body()?.takeIf { r.isSuccessful && it.success }?.data
            ?: throw Exception(r.body()?.error ?: "API ${r.code()}")
    }

    suspend fun syncPurchase(
        tokenId: Int,
        transactionHash: String,
        buyerAddress: String,
        price: String
    ): Result<MarketplaceItem> = runCatching {
        val req = SyncPurchaseRequest(tokenId, transactionHash, buyerAddress, price)
        val r = apiService.syncPurchase(req)
        r.body()?.takeIf { r.isSuccessful && it.success }?.data
            ?: throw Exception(r.body()?.error ?: "API ${r.code()}")
    }
}

// ─── User Repository ──────────────────────────────────────────────────────────

@Singleton
class UserRepository @Inject constructor(
    private val apiService: ChainTorqueApiService
) {
    suspend fun getUserNFTs(address: String): Result<List<UserNFT>> = runCatching {
        val r = apiService.getUserNFTs(address)
        r.body()?.takeIf { r.isSuccessful && it.success }?.data
            ?: throw Exception(r.body()?.error ?: "API ${r.code()}")
    }

    suspend fun getUserPurchases(address: String): Result<List<MarketplaceItem>> = runCatching {
        val r = apiService.getUserPurchases(address)
        r.body()?.takeIf { r.isSuccessful && it.success }?.data
            ?: throw Exception(r.body()?.error ?: "API ${r.code()}")
    }

    suspend fun getUserSales(address: String): Result<List<MarketplaceItem>> = runCatching {
        val r = apiService.getUserSales(address)
        r.body()?.takeIf { r.isSuccessful && it.success }?.data
            ?: throw Exception(r.body()?.error ?: "API ${r.code()}")
    }

    suspend fun registerUser(walletAddress: String): Result<UserProfile> = runCatching {
        val r = apiService.registerUser(mapOf("walletAddress" to walletAddress))
        r.body()?.takeIf { r.isSuccessful && it.success }?.data
            ?: throw Exception(r.body()?.error ?: "API ${r.code()}")
    }
}

// ─── Web3 Repository ──────────────────────────────────────────────────────────

/**
 * Handles Web3 operations.
 * ETH balance is fetched directly from the Sepolia public JSON-RPC node via OkHttp —
 * no WalletConnect relay round-trip, no third-party API keys required.
 */
@Singleton
class Web3Repository @Inject constructor(
    private val apiService: ChainTorqueApiService,
    private val okHttpClient: OkHttpClient
) {
    private companion object {
        const val TAG = "Web3Repository"
        const val SEPOLIA_RPC = "https://rpc.sepolia.org"
        val ETH_DECIMALS: BigDecimal = BigDecimal.TEN.pow(18)
    }

    /**
     * Returns a formatted ETH balance string (e.g. "0.0523").
     * Falls back to "0.0000" on any network/parse error.
     */
    suspend fun fetchEthBalance(address: String): String = withContext(Dispatchers.IO) {
        try {
            val payload =
                """{"jsonrpc":"2.0","method":"eth_getBalance","params":["$address","latest"],"id":1}"""
                    .toRequestBody("application/json".toMediaType())
            val req = okhttp3.Request.Builder().url(SEPOLIA_RPC).post(payload).build()

            okHttpClient.newCall(req).execute().use { resp ->
                if (!resp.isSuccessful) return@withContext "0.0000"
                val hex = JSONObject(resp.body!!.string()).getString("result")
                val wei = BigInteger(hex.removePrefix("0x"), 16)
                BigDecimal(wei).divide(ETH_DECIMALS, 4, RoundingMode.HALF_UP).toPlainString()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Balance fetch failed: ${e.message}")
            "0.0000"
        }
    }

    suspend fun getWeb3Status(): Result<Web3Status> = runCatching {
        val r = apiService.getWeb3Status()
        r.body()?.takeIf { r.isSuccessful && it.success }?.data
            ?: throw Exception(r.body()?.error ?: "API ${r.code()}")
    }

    fun isValidEthereumAddress(address: String): Boolean =
        address.matches(Regex("^0x[a-fA-F0-9]{40}$"))

    fun formatAddress(address: String): String =
        if (address.length > 10) "${address.take(6)}...${address.takeLast(4)}" else address
}
