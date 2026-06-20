package com.example.chaintorquenative.mobile.data.config

import com.example.chaintorquenative.BuildConfig
import java.math.BigDecimal

/**
 * Runtime app configuration — the single source of truth for the contract address.
 *
 * Defaults come from [BuildConfig] (compiled from local.properties) so the app works
 * offline and before the first network call returns. On launch the app fetches
 * `GET /api/config` and overwrites these with the backend's live values, which the
 * backend reads from the same `contract-address.json` the web app uses. This means a
 * contract redeploy is picked up automatically — no rebuild, no editing local.properties.
 */
object AppConfig {

    @Volatile
    var contractAddress: String = BuildConfig.CONTRACT_ADDRESS
        private set

    @Volatile
    var chainId: Long = 11155111L          // Sepolia
        private set

    /** Marketplace listing fee in ETH, charged on create and relist. */
    @Volatile
    var listingPriceEth: String = "0.00025"
        private set

    /** Apply values fetched from the backend, ignoring anything missing/invalid. */
    fun update(contractAddress: String?, chainId: Long?, listingPriceEth: String?) {
        contractAddress
            ?.takeIf { it.isNotBlank() && it.startsWith("0x") && it.length == 42 }
            ?.let { this.contractAddress = it }
        chainId?.takeIf { it > 0 }?.let { this.chainId = it }
        listingPriceEth?.takeIf { it.isNotBlank() }?.let { this.listingPriceEth = it }
    }

    /** Listing fee as a 0x-hex wei string, ready for the eth_sendTransaction `value`. */
    fun listingPriceWeiHex(): String {
        val wei = BigDecimal(listingPriceEth).multiply(BigDecimal.TEN.pow(18)).toBigInteger()
        return "0x" + wei.toString(16)
    }
}
