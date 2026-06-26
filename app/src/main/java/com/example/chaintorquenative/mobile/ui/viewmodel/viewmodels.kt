package com.example.chaintorquenative.mobile.ui.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.chaintorquenative.mobile.data.api.MarketplaceItem
import com.example.chaintorquenative.mobile.data.api.TransactionRecord
import com.example.chaintorquenative.mobile.data.api.UserNFT
import com.example.chaintorquenative.mobile.data.api.UserProfile
import com.example.chaintorquenative.mobile.data.config.AppConfig
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
                    item.description?.contains(query, ignoreCase = true) == true ||
                    item.category?.contains(query, ignoreCase = true) == true ||
                    item.username?.contains(query, ignoreCase = true) == true
            val matchCat = category == "All" || item.category.equals(category, ignoreCase = true)
            matchSearch && matchCat
        }
    }

    fun searchItems(query: String)       { _searchQuery.value = query; applyFilters() }
    fun selectCategory(category: String) { _selectedCategory.value = category; applyFilters() }
    fun selectItem(item: MarketplaceItem) { _selectedItem.value = item }
    fun clearError() { _error.value = null }

    /**
     * Executes the on-chain purchase after the user has confirmed in the UI.
     * Contract address is read from BuildConfig (sourced from local.properties — never hardcoded).
     */
    fun purchaseItem(tokenId: Int, buyerAddress: String, price: Double) {
        viewModelScope.launch {
            _loading.value = true
            _error.value   = null

            android.util.Log.d("MarketplaceVM", "purchaseItem tokenId=$tokenId buyer=$buyerAddress price=$price")

            // Pre-purchase sold check
            marketplaceRepository.getMarketplaceItem(tokenId).onSuccess { item ->
                if (item.status == "sold") {
                    _error.value = "This item has already been sold."
                    _loading.value = false
                    return@launch
                }
            }

            val data     = web3Repository.encodeBuyTokenData(tokenId)
            val valueHex = web3Repository.parseEthToWeiHex(price)

            walletConnectManager.sendTransaction(
                fromAddress = buyerAddress,
                toAddress   = AppConfig.contractAddress,
                data        = data,
                value       = valueHex,
                onSuccess   = { txHash ->
                    android.util.Log.d("MarketplaceVM", "Purchase success tx=$txHash")
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
    private val web3Repository: Web3Repository,
    private val marketplaceRepository: MarketplaceRepository,
    private val walletConnectManager: WalletConnectManager
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

    // Full transaction history (mints, purchases, sales, relists)
    private val _userTransactions = MutableLiveData<List<TransactionRecord>>(emptyList())
    val userTransactions: LiveData<List<TransactionRecord>> = _userTransactions

    private val _loading     = MutableLiveData<Boolean>()
    val loading: LiveData<Boolean> = _loading

    private val _error       = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    private val _isRefreshing = MutableLiveData(false)
    val isRefreshing: LiveData<Boolean> = _isRefreshing

    // Relist (resell) state
    private val _relisting = MutableLiveData(false)
    val relisting: LiveData<Boolean> = _relisting

    private val _relistMessage = MutableLiveData<String?>()
    val relistMessage: LiveData<String?> = _relistMessage

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

            // Register first to ensure the backend creates the user document if they are new
            try {
                val result = userRepository.registerUser(address)
                result.onSuccess { _userProfile.value = it }
                      .onFailure { android.util.Log.w("UserProfileVM", "register: ${it.message}") }
            } catch (e: Exception) {
                android.util.Log.w("UserProfileVM", "register failed: ${e.message}")
            }

            // Load purchases + created NFTs + sales concurrently AFTER registration
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
            val txJob = launch {
                userRepository.getUserTransactions(address)
                    .onSuccess { _userTransactions.postValue(it) }
                    .onFailure { android.util.Log.w("UserProfileVM", "transactions: ${it.message}") }
            }

            purchasesJob.join()
            nftsJob.join()
            salesJob.join()
            txJob.join()

            _loading.value     = false
            _isRefreshing.value = false
        }
    }

    fun refresh() {
        currentAddress?.let { _isRefreshing.value = true; loadUserData(it) }
    }

    /**
     * Re-lists an owned NFT back onto the marketplace at [newPriceEth].
     * Calls relistToken(uint256,uint128) on-chain (paying the listing fee), then
     * syncs the relist to the backend. Mirrors the purchase signing flow.
     */
    fun relistItem(tokenId: Int, sellerAddress: String, newPriceEth: Double) {
        if (newPriceEth <= 0.0) { _error.value = "Enter a price greater than 0"; return }
        viewModelScope.launch {
            _relisting.value = true
            _error.value     = null

            val data = web3Repository.encodeRelistTokenData(tokenId, newPriceEth)

            android.util.Log.d("UserProfileVM", "relistItem token=$tokenId price=$newPriceEth seller=$sellerAddress")

            walletConnectManager.sendTransaction(
                fromAddress = sellerAddress,
                toAddress   = AppConfig.contractAddress,
                data        = data,
                value       = AppConfig.listingPriceWeiHex(),  // listing fee, not the sale price
                onSuccess   = { txHash ->
                    android.util.Log.d("UserProfileVM", "Relist success tx=$txHash")
                    viewModelScope.launch {
                        marketplaceRepository.syncRelist(tokenId, txHash, sellerAddress, newPriceEth.toString())
                            .onSuccess {
                                _relistMessage.postValue("Listed for $newPriceEth ETH")
                                loadUserData(sellerAddress)
                            }
                            .onFailure {
                                _relistMessage.postValue("Relisted on-chain — sync pending")
                                android.util.Log.e("UserProfileVM", "Relist sync failed: ${it.message}")
                            }
                        _relisting.postValue(false)
                    }
                },
                onError = { msg ->
                    _error.postValue("Relist failed: $msg")
                    _relisting.postValue(false)
                }
            )
        }
    }

    fun clearError() { _error.value = null }
    fun clearRelistMessage() { _relistMessage.value = null }
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
