package com.example.chaintorquenative.mobile.ui.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.chaintorquenative.mobile.data.repository.MarketplaceRepository
import com.example.chaintorquenative.mobile.data.repository.UserRepository
import com.example.chaintorquenative.mobile.data.repository.Web3Repository
import com.example.chaintorquenative.mobile.data.api.MarketplaceItem
import com.example.chaintorquenative.mobile.data.api.UserNFT
import com.example.chaintorquenative.mobile.data.api.UserProfile
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MarketplaceViewModel @Inject constructor(
    private val marketplaceRepository: MarketplaceRepository,
    private val web3Repository: Web3Repository
) : ViewModel() {

    private val _marketplaceItems = MutableLiveData<List<MarketplaceItem>>()
    val marketplaceItems: LiveData<List<MarketplaceItem>> = _marketplaceItems

    private val _selectedItem = MutableLiveData<MarketplaceItem?>()
    val selectedItem: LiveData<MarketplaceItem?> = _selectedItem

    private val _loading = MutableLiveData<Boolean>()
    val loading: LiveData<Boolean> = _loading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    private val _purchaseSuccess = MutableLiveData<String?>()
    val purchaseSuccess: LiveData<String?> = _purchaseSuccess

    private val _searchQuery = MutableLiveData<String>("")
    val searchQuery: LiveData<String> = _searchQuery

    private val _filteredItems = MutableLiveData<List<MarketplaceItem>>()
    val filteredItems: LiveData<List<MarketplaceItem>> = _filteredItems

    init {
        loadMarketplaceItems()
    }

    fun loadMarketplaceItems() {
        viewModelScope.launch {
            _loading.value = true
            _error.value = null

            marketplaceRepository.getMarketplaceItems()
                .onSuccess { items ->
                    _marketplaceItems.value = items
                    filterItems(_searchQuery.value ?: "")
                }
                .onFailure { exception ->
                    _error.value = exception.message
                }

            _loading.value = false
        }
    }

    private fun filterItems(query: String) {
        val items = _marketplaceItems.value ?: emptyList()
        if (query.isBlank()) {
            _filteredItems.value = items
        } else {
            _filteredItems.value = items.filter { item ->
                item.title?.contains(query, ignoreCase = true) == true ||
                        item.description?.contains(query, ignoreCase = true) == true ||
                        item.seller?.contains(query, ignoreCase = true) == true
            }
        }
    }

    fun searchItems(query: String) {
        _searchQuery.value = query
        filterItems(query)
    }

    fun selectItem(item: MarketplaceItem) {
        _selectedItem.value = item
    }

    fun loadItemDetails(tokenId: Int) {
        viewModelScope.launch {
            _loading.value = true

            marketplaceRepository.getMarketplaceItem(tokenId)
                .onSuccess { item ->
                    _selectedItem.value = item
                }
                .onFailure { exception ->
                    _error.value = exception.message
                }

            _loading.value = false
        }
    }

    // Note: Actual NFT purchase requires WalletConnect for signing transactions
    // This method prepares the purchase and would sync after blockchain confirmation
    fun purchaseItem(tokenId: Int, buyerAddress: String, price: Double) {
        viewModelScope.launch {
            _loading.value = true
            _error.value = null
            _purchaseSuccess.value = null

            // TODO: Integrate WalletConnect here for actual on-chain purchase
            // For now, just show a message that WalletConnect is needed
            _error.value = "WalletConnect integration needed for purchases"

            _loading.value = false
        }
    }

    fun clearError() {
        _error.value = null
    }

    fun clearPurchaseSuccess() {
        _purchaseSuccess.value = null
    }
}

@HiltViewModel
class UserProfileViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val web3Repository: Web3Repository
) : ViewModel() {

    enum class ProfileTab { OWNED, PURCHASES, SALES }

    private val _userProfile = MutableLiveData<UserProfile?>()
    val userProfile: LiveData<UserProfile?> = _userProfile

    private val _userNFTs = MutableLiveData<List<UserNFT>>()
    val userNFTs: LiveData<List<UserNFT>> = _userNFTs

    private val _userPurchases = MutableLiveData<List<MarketplaceItem>>()
    val userPurchases: LiveData<List<MarketplaceItem>> = _userPurchases

    private val _userSales = MutableLiveData<List<MarketplaceItem>>()
    val userSales: LiveData<List<MarketplaceItem>> = _userSales

    private val _walletBalance = MutableLiveData<String>("")
    val walletBalance: LiveData<String> = _walletBalance

    private val _loading = MutableLiveData<Boolean>()
    val loading: LiveData<Boolean> = _loading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    private val _currentTab = MutableLiveData<ProfileTab>(ProfileTab.OWNED)
    val currentTab: LiveData<ProfileTab> = _currentTab

    fun loadUserData(address: String) {
        viewModelScope.launch {
            _loading.value = true
            _error.value = null

            // Register/get user profile
            userRepository.registerUser(address)
                .onSuccess { profile ->
                    _userProfile.value = profile
                }
                .onFailure { exception ->
                    // Profile failure is not critical
                }

            // Load user NFTs
            loadUserNFTs(address)

            _loading.value = false
        }
    }

    fun loadUserNFTs(address: String) {
        viewModelScope.launch {
            userRepository.getUserNFTs(address)
                .onSuccess { nfts ->
                    _userNFTs.value = nfts
                }
                .onFailure { exception ->
                    _error.value = exception.message
                }
        }
    }

    fun loadUserPurchases(address: String) {
        viewModelScope.launch {
            _loading.value = true

            userRepository.getUserPurchases(address)
                .onSuccess { purchases ->
                    _userPurchases.value = purchases
                }
                .onFailure { exception ->
                    _error.value = exception.message
                }

            _loading.value = false
        }
    }

    fun loadUserSales(address: String) {
        viewModelScope.launch {
            _loading.value = true

            userRepository.getUserSales(address)
                .onSuccess { sales ->
                    _userSales.value = sales
                }
                .onFailure { exception ->
                    _error.value = exception.message
                }

            _loading.value = false
        }
    }

    fun setCurrentTab(tab: ProfileTab) {
        _currentTab.value = tab
    }

    fun clearError() {
        _error.value = null
    }
}

@HiltViewModel
class WalletViewModel @Inject constructor(
    private val web3Repository: Web3Repository
) : ViewModel() {

    enum class ConnectionStatus { DISCONNECTED, CONNECTING, CONNECTED, ERROR }

    private val _walletAddress = MutableLiveData<String?>()
    val walletAddress: LiveData<String?> = _walletAddress

    private val _isConnected = MutableLiveData<Boolean>(false)
    val isConnected: LiveData<Boolean> = _isConnected

    private val _balance = MutableLiveData<String>("")
    val balance: LiveData<String> = _balance

    private val _loading = MutableLiveData<Boolean>()
    val loading: LiveData<Boolean> = _loading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    private val _connectionStatus = MutableLiveData<ConnectionStatus>(ConnectionStatus.DISCONNECTED)
    val connectionStatus: LiveData<ConnectionStatus> = _connectionStatus

    fun connectWallet(address: String) {
        viewModelScope.launch {
            _loading.value = true
            _connectionStatus.value = ConnectionStatus.CONNECTING
            _error.value = null

            // Validate address format (client-side)
            if (web3Repository.isValidEthereumAddress(address)) {
                _walletAddress.value = address
                _isConnected.value = true
                _connectionStatus.value = ConnectionStatus.CONNECTED
                // Note: Balance would be fetched from blockchain, for now just set placeholder
                _balance.value = "0.0"
            } else {
                _error.value = "Invalid wallet address format"
                _connectionStatus.value = ConnectionStatus.ERROR
            }

            _loading.value = false
        }
    }

    fun disconnectWallet() {
        _walletAddress.value = null
        _isConnected.value = false
        _balance.value = ""
        _connectionStatus.value = ConnectionStatus.DISCONNECTED
        _error.value = null
    }

    fun loadBalance() {
        val address = _walletAddress.value ?: return

        viewModelScope.launch {
            // Note: Would integrate with WalletConnect/Web3 to get real balance
            // For now just use placeholder
            _balance.value = "0.0"
        }
    }

    fun checkWeb3Status() {
        viewModelScope.launch {
            web3Repository.getWeb3Status()
                .onSuccess {
                    // Web3 is available
                }
                .onFailure {
                    // Web3 not available
                }
        }
    }

    fun clearError() {
        _error.value = null
    }
}
