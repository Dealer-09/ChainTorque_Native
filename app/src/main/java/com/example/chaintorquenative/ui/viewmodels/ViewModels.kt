package com.example.chaintorquenative.ui.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor() : ViewModel() {
    
    private val _currentScreen = MutableLiveData<Screen>(Screen.MARKETPLACE)
    val currentScreen: LiveData<Screen> = _currentScreen
    
    enum class Screen { MARKETPLACE, PROFILE, WALLET, SETTINGS }
    
    fun navigateToScreen(screen: Screen) {
        _currentScreen.value = screen
    }
}

@HiltViewModel
class WalletViewModel @Inject constructor() : ViewModel() {
    
    private val _walletAddress = MutableLiveData<String?>()
    val walletAddress: LiveData<String?> = _walletAddress
    
    private val _isConnected = MutableLiveData<Boolean>(false)
    val isConnected: LiveData<Boolean> = _isConnected
    
    fun connectWallet(address: String) {
        _walletAddress.value = address
        _isConnected.value = true
    }
    
    fun disconnectWallet() {
        _walletAddress.value = null
        _isConnected.value = false
    }
}