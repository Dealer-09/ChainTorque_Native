package com.example.chaintorquenative.mobile.ui.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.chaintorquenative.BuildConfig
import com.example.chaintorquenative.mobile.data.api.MarketplaceItem
import com.example.chaintorquenative.mobile.data.api.UserNFT
import com.example.chaintorquenative.mobile.data.api.UserProfile
import com.example.chaintorquenative.mobile.data.repository.MarketplaceRepository
import com.example.chaintorquenative.mobile.data.repository.UserRepository
import com.example.chaintorquenative.mobile.data.repository.Web3Repository
import com.example.chaintorquenative.wallet.WalletConnectManager
import com.example.chaintorquenative.wallet.WalletConnectionState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

// =============================================================================
// MARKETPLACE VIEW MODEL
// =============================================================================

@HiltViewModel
class MarketplaceViewModel @Inject constructor(
    private val marketplaceRepository: MarketplaceRepository,
    private val web3Repository: Web3Repository,
    private val walletConnectManager: WalletConnectManager
) : ViewModel() {

    private val _marketplaceItems  = MutableLiveData<List<MarketplaceItem>>()
    val marketplaceItems: LiveData<List<MarketplaceItem>> = _marketplaceItems

    private val _selectedItem = MutableLiveData<MarketplaceItem?>()
    val selectedItem: LiveData<MarketplaceItem?> = _selectedItem

    private val _loading = MutableLiveData<Boolean>()
    val loading: LiveData<Boolean> = _loading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    private val _purchaseSuccess = MutableLiveData<String?>()
    val purchaseSuccess: LiveData<String?> = _purchaseSuccess

    private val _isRefreshing = MutableLiveData(false)
    val isRefreshing: LiveData<Boolean> = _isRefreshing

    private val _searchQuery = MutableLiveData("")
    val searchQuery: LiveData<String> = _searchQuery

    private val _filteredItems = MutableLiveData<List<MarketplaceItem>>()
    val filteredItems: LiveData<List<MarketplaceItem>> = _filteredItems

    private val _selectedCategory = MutableLiveData("All")
    val selectedCategory: LiveData<String> = _selectedCategory

    init { loadMarketplaceItems() }

    fun loadMarketplaceItems() {
        viewModelScope.launch {
            _loading.value = true
            _error.value   = null
            marketplaceRepository.getMarketplaceItems()
                .onSuccess { items -> _marketplaceItems.value = items; applyFilters() }
                .onFailure { _error.value = it.message }
            _loading.value     = false
            _isRefreshing.value = false
        }
    }

    fun refresh() { _isRefreshing.value = true; loadMarketplaceItems() }

    private fun applyFilters() {
        val items    = _marketplaceItems.value ?: emptyList()
        val query    = _searchQuery.value ?: ""
        val category = _selectedCategory.value ?: "All"
        _filteredItems.value = items.filter { item ->
            val matchSearch = query.isBlank() ||
                    item.title?.contains(query, ignoreCase = true) == true ||
                    item.description?.contains(query, ignoreCase = true) == true
            val matchCat = category == "All" || item.category.equals(category, ignoreCase = true)
            matchSearch && matchCat
        }
    }

    fun searchItems(query: String)       { _searchQuery.value = query; applyFilters() }
    fun selectCategory(category: String) { _selectedCategory.value = category; applyFilters() }
    fun selectItem(item: MarketplaceItem) { _selectedItem.value = item }
    fun clearError()           { _error.value = null }
    fun clearPurchaseSuccess() { _purchaseSuccess.value = null }

    fun loadItemDetails(tokenId: Int) {
        viewModelScope.launch {
            _loading.value = true
            marketplaceRepository.getMarketplaceItem(tokenId)
                .onSuccess { _selectedItem.value = it }
                .onFailure { _error.value = it.message }
            _loading.value = false
        }
    }

    /**
     * Executes the on-chain purchase after the user has confirmed in the UI.
     * Contract address is read from BuildConfig (sourced from local.properties — never hardcoded).
     */
    fun purchaseItem(tokenId: Int, buyerAddress: String, price: Double) {
        viewModelScope.launch {
            _loading.value = true
            _error.value   = null
            _purchaseSuccess.value = null

            android.util.Log.d("MarketplaceVM", "purchaseItem tokenId=$tokenId buyer=$buyerAddress price=$price")

            // Pre-purchase sold check
            marketplaceRepository.getMarketplaceItem(tokenId).onSuccess { item ->
                if (item.status == "sold") {
                    _error.value = "This item has already been sold."
                    _loading.value = false
                    return@launch
                }
            }

            val functionSelector = "0xc2db2c42"
            val paddedTokenId    = "%064x".format(tokenId)
            val data             = functionSelector + paddedTokenId

            val priceWei  = java.math.BigDecimal(price).multiply(java.math.BigDecimal.TEN.pow(18)).toBigInteger()
            val valueHex  = "0x" + priceWei.toString(16)

            walletConnectManager.sendTransaction(
                fromAddress = buyerAddress,
                toAddress   = BuildConfig.CONTRACT_ADDRESS,
                data        = data,
                value       = valueHex,
                onSuccess   = { txHash ->
                    _purchaseSuccess.postValue(txHash)
                    _loading.postValue(false)
                    viewModelScope.launch {
                        marketplaceRepository.syncPurchase(tokenId, txHash, buyerAddress, price.toString())
                            .onSuccess { loadMarketplaceItems() }
                            .onFailure { android.util.Log.e("MarketplaceVM", "Sync failed: ${it.message}") }
                    }
                },
                onError = { msg ->
                    _error.postValue("Purchase failed: $msg")
                    _loading.postValue(false)
                }
            )
        }
    }
}

// =============================================================================
// USER PROFILE VIEW MODEL
// =============================================================================

@HiltViewModel
class UserProfileViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val web3Repository: Web3Repository
) : ViewModel() {

    private val _userProfile   = MutableLiveData<UserProfile?>()
    val userProfile: LiveData<UserProfile?> = _userProfile

    private val _userPurchases = MutableLiveData<List<MarketplaceItem>>(emptyList())
    val userPurchases: LiveData<List<MarketplaceItem>> = _userPurchases

    // User's created/minted NFTs
    private val _userNFTs = MutableLiveData<List<UserNFT>>(emptyList())
    val userNFTs: LiveData<List<UserNFT>> = _userNFTs

    // Items the user has sold
    private val _userSales = MutableLiveData<List<MarketplaceItem>>(emptyList())
    val userSales: LiveData<List<MarketplaceItem>> = _userSales

    private val _loading     = MutableLiveData<Boolean>()
    val loading: LiveData<Boolean> = _loading

    private val _error       = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    private val _isRefreshing = MutableLiveData(false)
    val isRefreshing: LiveData<Boolean> = _isRefreshing

    private var currentAddress: String? = null

    /**
     * Loads all user data in parallel:
     * - Registers user to backend
     * - Loads purchased items  (FIX #2 already worked, kept here)
     * - Loads created NFTs     (FIX #2: was never called before)
     */
    fun loadUserData(address: String) {
        android.util.Log.d("UserProfileVM", "loadUserData address=$address")
        currentAddress = address
        viewModelScope.launch {
            _loading.value = true
            _error.value   = null

            // Register (fire-and-forget)
            userRepository.registerUser(address)
                .onSuccess { _userProfile.value = it }
                .onFailure { android.util.Log.w("UserProfileVM", "register: ${it.message}") }

            // Load purchases + created NFTs + sales concurrently
            val purchasesJob = launch {
                userRepository.getUserPurchases(address)
                    .onSuccess { _userPurchases.postValue(it) }
                    .onFailure { _error.postValue(it.message) }
            }
            val nftsJob = launch {
                userRepository.getUserNFTs(address)
                    .onSuccess { _userNFTs.postValue(it) }
                    .onFailure { android.util.Log.w("UserProfileVM", "nfts: ${it.message}") }
            }
            val salesJob = launch {
                userRepository.getUserSales(address)
                    .onSuccess { _userSales.postValue(it) }
                    .onFailure { android.util.Log.w("UserProfileVM", "sales: ${it.message}") }
            }

            purchasesJob.join()
            nftsJob.join()
            salesJob.join()

            _loading.value     = false
            _isRefreshing.value = false
        }
    }

    fun refresh() {
        currentAddress?.let { _isRefreshing.value = true; loadUserData(it) }
    }

    fun clearError() { _error.value = null }
}

// =============================================================================
// WALLET VIEW MODEL
// =============================================================================

@HiltViewModel
class WalletViewModel @Inject constructor(
    private val web3Repository: Web3Repository,
    private val walletConnectManager: WalletConnectManager
) : ViewModel() {

    enum class ConnectionStatus { DISCONNECTED, CONNECTING, CONNECTED, ERROR }

    private val _walletAddress    = MutableLiveData<String?>()
    val walletAddress: LiveData<String?> = _walletAddress

    private val _isConnected      = MutableLiveData(false)
    val isConnected: LiveData<Boolean> = _isConnected

    // ── FIX #1 & #3: balance is now fetched via eth_getBalance after connect ──
    private val _balance          = MutableLiveData("")
    val balance: LiveData<String> = _balance

    private val _loading          = MutableLiveData<Boolean>()
    val loading: LiveData<Boolean> = _loading

    private val _error            = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    private val _connectionStatus = MutableLiveData(ConnectionStatus.DISCONNECTED)
    val connectionStatus: LiveData<ConnectionStatus> = _connectionStatus

    private val _chainName        = MutableLiveData("Sepolia")
    val chainName: LiveData<String> = _chainName

    init {
        viewModelScope.launch {
            walletConnectManager.connectionState.collect { state ->
                when (state) {
                    is WalletConnectionState.Disconnected -> {
                        _connectionStatus.value = ConnectionStatus.DISCONNECTED
                        _isConnected.value      = false
                        _walletAddress.value    = null
                        _balance.value          = ""
                    }
                    is WalletConnectionState.Connecting -> {
                        _connectionStatus.value = ConnectionStatus.CONNECTING
                        _loading.value          = true
                    }
                    is WalletConnectionState.Connected -> {
                        _connectionStatus.value = ConnectionStatus.CONNECTED
                        _isConnected.value      = true
                        _walletAddress.value    = state.address
                        _loading.value          = false
                        _chainName.value        = if (state.chainId.contains("11155111")) "Sepolia" else "Unknown"

                        // ── Fetch real on-chain balance from Sepolia RPC ──────
                        viewModelScope.launch {
                            val bal = web3Repository.fetchEthBalance(state.address)
                            _balance.postValue(bal)
                            android.util.Log.d("WalletVM", "Balance fetched: $bal ETH")
                        }
                    }
                    is WalletConnectionState.Error -> {
                        _connectionStatus.value = ConnectionStatus.ERROR
                        _error.value            = state.message
                        _loading.value          = false
                    }
                }
            }
        }
    }

    fun prepareConnect() {
        _loading.value          = true
        _connectionStatus.value = ConnectionStatus.CONNECTING
        _error.value            = null
        walletConnectManager.prepareConnect()
    }

    fun disconnectWallet() {
        walletConnectManager.disconnect()
        _walletAddress.value    = null
        _isConnected.value      = false
        _balance.value          = ""
        _connectionStatus.value = ConnectionStatus.DISCONNECTED
        _error.value            = null
    }

    fun clearError() { _error.value = null }
}
