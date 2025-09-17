
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
    
    fun searchItems(query: String) {
        _searchQuery.value = query
        filterItems(query)
    }
    
    private fun filterItems(query: String) {
        val items = _marketplaceItems.value ?: return
        
        if (query.isBlank()) {
            _filteredItems.value = items
        } else {
            _filteredItems.value = items.filter { item ->
                item.title.contains(query, ignoreCase = true) ||
                item.description.contains(query, ignoreCase = true) ||
                item.seller.name.contains(query, ignoreCase = true)
            }
        }
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
    
    fun purchaseItem(tokenId: Int, buyerAddress: String, price: Double) {
        viewModelScope.launch {
            _loading.value = true
            _error.value = null
            _purchaseSuccess.value = null
            
            marketplaceRepository.purchaseNFT(tokenId, buyerAddress, price)
                .onSuccess { transactionHash ->
                    _purchaseSuccess.value = transactionHash
                    loadMarketplaceItems() // Refresh items after purchase
                }
                .onFailure { exception ->
                    _error.value = exception.message
                }
            
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
    
    private val _userProfile = MutableLiveData<UserProfile?>()
    val userProfile: LiveData<UserProfile?> = _userProfile
    
    private val _userNFTs = MutableLiveData<List<UserNFT>>()
    val userNFTs: LiveData<List<UserNFT>> = _userNFTs
    
    private val _userPurchases = MutableLiveData<List<MarketplaceItem>>()
    val userPurchases: LiveData<List<MarketplaceItem>> = _userPurchases
    
    private val _userSales = MutableLiveData<List<MarketplaceItem>>()
    val userSales: LiveData<List<MarketplaceItem>> = _userSales
    
    private val _walletBalance = MutableLiveData<String>()
    val walletBalance: LiveData<String> = _walletBalance
    
    private val _loading = MutableLiveData<Boolean>()
    val loading: LiveData<Boolean> = _loading
    
    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error
    
    private val _currentTab = MutableLiveData<ProfileTab>(ProfileTab.OWNED)
    val currentTab: LiveData<ProfileTab> = _currentTab
    
    enum class ProfileTab { OWNED, PURCHASES, SALES }
    
    fun loadUserData(address: String) {
        viewModelScope.launch {
            _loading.value = true
            _error.value = null
            
            // Load profile
            userRepository.getUserProfile(address)
                .onSuccess { profile ->
                    _userProfile.value = profile
                }
                .onFailure { exception ->
                    _error.value = exception.message
                }
            
            // Load wallet balance
            web3Repository.getBalance(address)
                .onSuccess { balance ->
                    _walletBalance.value = balance
                }
                .onFailure { exception ->
                    // Balance failure is not critical
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
            userRepository.getUserPurchases(address)
                .onSuccess { purchases ->
                    _userPurchases.value = purchases
                }
                .onFailure { exception ->
                    _error.value = exception.message
                }
        }
    }
    
    fun loadUserSales(address: String) {
        viewModelScope.launch {
            userRepository.getUserSales(address)
                .onSuccess { sales ->
                    _userSales.value = sales
                }
                .onFailure { exception ->
                    _error.value = exception.message
                }
        }
    }
    
    fun setCurrentTab(tab: ProfileTab) {
        _currentTab.value = tab
    }
    
    fun refreshUserData(address: String) {
        when (_currentTab.value) {
            ProfileTab.OWNED -> loadUserNFTs(address)
            ProfileTab.PURCHASES -> loadUserPurchases(address)
            ProfileTab.SALES -> loadUserSales(address)
            null -> loadUserData(address)
        }
    }
    
    fun clearError() {
        _error.value = null
    }
}

@HiltViewModel
class WalletViewModel @Inject constructor(
    private val web3Repository: Web3Repository
) : ViewModel() {
    
    private val _walletAddress = MutableLiveData<String?>()
    val walletAddress: LiveData<String?> = _walletAddress
    
    private val _isConnected = MutableLiveData<Boolean>(false)
    val isConnected: LiveData<Boolean> = _isConnected
    
    private val _balance = MutableLiveData<String>()
    val balance: LiveData<String> = _balance
    
    private val _loading = MutableLiveData<Boolean>()
    val loading: LiveData<Boolean> = _loading
    
    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error
    
    private val _connectionStatus = MutableLiveData<ConnectionStatus>()
    val connectionStatus: LiveData<ConnectionStatus> = _connectionStatus
    
    enum class ConnectionStatus { DISCONNECTED, CONNECTING, CONNECTED, ERROR }
    
    fun connectWallet(address: String) {
        viewModelScope.launch {
            _loading.value = true
            _connectionStatus.value = ConnectionStatus.CONNECTING
            _error.value = null
            
            // Validate address format
            web3Repository.validateAddress(address)
                .onSuccess { isValid ->
                    if (isValid) {
                        _walletAddress.value = address
                        _isConnected.value = true
                        _connectionStatus.value = ConnectionStatus.CONNECTED
                        loadBalance(address)
                    } else {
                        _error.value = "Invalid wallet address format"
                        _connectionStatus.value = ConnectionStatus.ERROR
                    }
                }
                .onFailure { exception ->
                    _error.value = exception.message
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
    
    fun loadBalance(address: String? = _walletAddress.value) {
        if (address == null) return
        
        viewModelScope.launch {
            web3Repository.getBalance(address)
                .onSuccess { balance ->
                    _balance.value = balance
                }
                .onFailure { exception ->
                    _error.value = exception.message
                }
        }
    }
    
    fun checkWeb3Status() {
        viewModelScope.launch {
            web3Repository.getWeb3Status()
                .onSuccess {
                    // Web3 is available
                }
                .onFailure { exception ->
                    _error.value = "Web3 not available: ${exception.message}"
                }
        }
    }
    
    fun clearError() {
        _error.value = null
    }
}

@HiltViewModel
class MainViewModel @Inject constructor() : ViewModel() {
    
    private val _currentScreen = MutableLiveData<Screen>(Screen.MARKETPLACE)
    val currentScreen: LiveData<Screen> = _currentScreen
    
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading
    
    private val _showBottomSheet = MutableLiveData<Boolean>()
    val showBottomSheet: LiveData<Boolean> = _showBottomSheet
    
    enum class Screen { MARKETPLACE, PROFILE, WALLET, SETTINGS }
    
    fun navigateToScreen(screen: Screen) {
        _currentScreen.value = screen
    }
    
    fun setLoading(loading: Boolean) {
        _isLoading.value = loading
    }
    
    fun showBottomSheet() {
        _showBottomSheet.value = true
    }
    
    fun hideBottomSheet() {
        _showBottomSheet.value = false
    }
}
