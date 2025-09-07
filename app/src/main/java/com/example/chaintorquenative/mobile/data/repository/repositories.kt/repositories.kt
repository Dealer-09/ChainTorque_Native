// Repository Pattern for ChainTorque Mobile App
// Handles data operations and API calls

package com.chaintorque.mobile.data.repository

import com.chaintorque.mobile.data.api.ChainTorqueApiService
import com.chaintorque.mobile.data.api.MarketplaceItem
import com.chaintorque.mobile.data.api.UserNFT
import com.chaintorque.mobile.data.api.UserProfile
import com.chaintorque.mobile.data.api.PurchaseRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MarketplaceRepository @Inject constructor(
    private val apiService: ChainTorqueApiService
) {
    
    // Get all marketplace items
    suspend fun getMarketplaceItems(): Result<List<MarketplaceItem>> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getMarketplaceItems()
            if (response.isSuccessful && response.body()?.success == true) {
                Result.success(response.body()?.data ?: emptyList())
            } else {
                Result.failure(Exception(response.body()?.error ?: "Failed to load marketplace items"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    // Get specific marketplace item
    suspend fun getMarketplaceItem(tokenId: Int): Result<MarketplaceItem> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getMarketplaceItem(tokenId)
            if (response.isSuccessful && response.body()?.success == true) {
                response.body()?.data?.let {
                    Result.success(it)
                } ?: Result.failure(Exception("Item not found"))
            } else {
                Result.failure(Exception(response.body()?.error ?: "Failed to load item"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    // Purchase NFT
    suspend fun purchaseNFT(tokenId: Int, buyerAddress: String, price: Double): Result<String> = withContext(Dispatchers.IO) {
        try {
            val request = PurchaseRequest(tokenId, buyerAddress, price)
            val response = apiService.purchaseNFT(request)
            if (response.isSuccessful && response.body()?.success == true) {
                Result.success(response.body()?.data?.transactionHash ?: "")
            } else {
                Result.failure(Exception(response.body()?.error ?: "Purchase failed"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    // Get marketplace stats
    suspend fun getMarketplaceStats(): Result<Any> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getMarketplaceStats()
            if (response.isSuccessful && response.body()?.success == true) {
                Result.success(response.body()?.data ?: Any())
            } else {
                Result.failure(Exception(response.body()?.error ?: "Failed to load stats"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

@Singleton
class UserRepository @Inject constructor(
    private val apiService: ChainTorqueApiService
) {
    
    // Get user's NFTs
    suspend fun getUserNFTs(address: String): Result<List<UserNFT>> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getUserNFTs(address)
            if (response.isSuccessful && response.body()?.success == true) {
                Result.success(response.body()?.data ?: emptyList())
            } else {
                Result.failure(Exception(response.body()?.error ?: "Failed to load user NFTs"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    // Get user's purchase history
    suspend fun getUserPurchases(address: String): Result<List<MarketplaceItem>> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getUserPurchases(address)
            if (response.isSuccessful && response.body()?.success == true) {
                Result.success(response.body()?.data ?: emptyList())
            } else {
                Result.failure(Exception(response.body()?.error ?: "Failed to load purchases"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    // Get user's sales history
    suspend fun getUserSales(address: String): Result<List<MarketplaceItem>> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getUserSales(address)
            if (response.isSuccessful && response.body()?.success == true) {
                Result.success(response.body()?.data ?: emptyList())
            } else {
                Result.failure(Exception(response.body()?.error ?: "Failed to load sales"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    // Get user profile
    suspend fun getUserProfile(address: String): Result<UserProfile> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getUserProfile(address)
            if (response.isSuccessful && response.body()?.success == true) {
                response.body()?.data?.let {
                    Result.success(it)
                } ?: Result.failure(Exception("Profile not found"))
            } else {
                Result.failure(Exception(response.body()?.error ?: "Failed to load profile"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

@Singleton
class Web3Repository @Inject constructor(
    private val apiService: ChainTorqueApiService
) {
    
    // Check Web3 connection status
    suspend fun getWeb3Status(): Result<Any> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getWeb3Status()
            if (response.isSuccessful && response.body()?.success == true) {
                Result.success(response.body()?.data ?: Any())
            } else {
                Result.failure(Exception(response.body()?.error ?: "Web3 not connected"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    // Validate wallet address
    suspend fun validateAddress(address: String): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            val request = mapOf("address" to address)
            val response = apiService.validateAddress(request)
            if (response.isSuccessful && response.body()?.success == true) {
                val isValid = response.body()?.data?.get("valid") ?: false
                Result.success(isValid)
            } else {
                Result.failure(Exception(response.body()?.error ?: "Validation failed"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    // Get wallet balance
    suspend fun getBalance(address: String): Result<String> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getBalance(address)
            if (response.isSuccessful && response.body()?.success == true) {
                val balance = response.body()?.data?.get("balance") ?: "0"
                Result.success(balance)
            } else {
                Result.failure(Exception(response.body()?.error ?: "Failed to get balance"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

/*
Usage in ViewModel:

class MarketplaceViewModel @Inject constructor(
    private val marketplaceRepository: MarketplaceRepository
) : ViewModel() {
    
    private val _marketplaceItems = MutableLiveData<List<MarketplaceItem>>()
    val marketplaceItems: LiveData<List<MarketplaceItem>> = _marketplaceItems
    
    private val _loading = MutableLiveData<Boolean>()
    val loading: LiveData<Boolean> = _loading
    
    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error
    
    fun loadMarketplaceItems() {
        viewModelScope.launch {
            _loading.value = true
            marketplaceRepository.getMarketplaceItems()
                .onSuccess { items ->
                    _marketplaceItems.value = items
                    _error.value = null
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
            marketplaceRepository.purchaseNFT(tokenId, buyerAddress, price)
                .onSuccess { transactionHash ->
                    // Handle successful purchase
                    _error.value = null
                }
                .onFailure { exception ->
                    _error.value = exception.message
                }
            _loading.value = false
        }
    }
}
*/
