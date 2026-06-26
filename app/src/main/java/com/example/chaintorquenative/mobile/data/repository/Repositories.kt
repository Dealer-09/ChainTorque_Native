package com.example.chaintorquenative.mobile.data.repository

import android.util.Log
import com.example.chaintorquenative.BuildConfig
import com.example.chaintorquenative.mobile.data.api.*
import com.example.chaintorquenative.mobile.data.config.AppConfig
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

    suspend fun syncRelist(
        tokenId: Int,
        transactionHash: String,
        sellerAddress: String,
        price: String
    ): Result<MarketplaceItem> = runCatching {
        val req = SyncRelistRequest(tokenId, transactionHash, sellerAddress, price)
        val r = apiService.syncRelist(req)
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

    suspend fun getUserTransactions(address: String): Result<List<TransactionRecord>> = runCatching {
        val r = apiService.getUserTransactions(address)
        r.body()?.takeIf { r.isSuccessful && it.success }?.data
            ?: throw Exception(r.body()?.error ?: "API ${r.code()}")
    }

    suspend fun registerUser(walletAddress: String): Result<UserProfile> = runCatching {
        val r = apiService.registerUser(mapOf("walletAddress" to walletAddress))
        r.body()?.takeIf { r.isSuccessful && it.success }?.data
            ?: throw Exception(r.body()?.error ?: "API ${r.code()}")
    }
}

// ─── Config Repository ────────────────────────────────────────────────────────

/**
 * Fetches runtime config (contract address, chain, listing price) from the backend
 * and applies it to [AppConfig]. Called once on app launch. Any failure is non-fatal —
 * the app keeps the compiled-in BuildConfig defaults.
 */
@Singleton
class ConfigRepository @Inject constructor(
    private val apiService: ChainTorqueApiService
) {
    suspend fun loadConfig() {
        runCatching {
            val r = apiService.getConfig()
            val body = r.body()
            if (r.isSuccessful && body != null && body.success) {
                AppConfig.update(body.contractAddress, body.chainId, body.listingPrice)
                Log.d("ConfigRepository", "Config loaded: contract=${AppConfig.contractAddress} chain=${AppConfig.chainId} fee=${AppConfig.listingPriceEth}")
            } else {
                Log.w("ConfigRepository", "Config response not OK (${r.code()}); using BuildConfig defaults")
            }
        }.onFailure {
            Log.w("ConfigRepository", "Config fetch failed; using BuildConfig defaults: ${it.message}")
        }
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
        val SEPOLIA_RPC: String = BuildConfig.RPC_URL
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

    /**
     * Encodes the calldata for buyToken(uint256)
     * Function Selector: 0xc2db2c42
     */
    fun encodeBuyTokenData(tokenId: Int): String {
        val functionSelector = "0xc2db2c42"
        val paddedTokenId = "%064x".format(tokenId)
        return functionSelector + paddedTokenId
    }

    /**
     * Encodes the calldata for relistToken(uint256,uint128)
     * Function Selector: 0x417c7275
     */
    fun encodeRelistTokenData(tokenId: Int, priceEth: Double): String {
        val selector = "0x417c7275"
        val paddedTokenId = "%064x".format(tokenId)
        val priceWei = BigDecimal(priceEth).multiply(ETH_DECIMALS).toBigInteger()
        val paddedPrice = "%064x".format(priceWei)
        return selector + paddedTokenId + paddedPrice
    }

    /**
     * Converts an ETH decimal value to a Wei Hex string (e.g. for transaction value)
     */
    fun parseEthToWeiHex(ethValue: Double): String {
        val wei = BigDecimal(ethValue).multiply(ETH_DECIMALS).toBigInteger()
        return "0x" + wei.toString(16)
    }
}
