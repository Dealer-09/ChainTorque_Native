package com.example.chaintorquenative.ui.screens.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.compose.ui.platform.LocalLifecycleOwner
import com.example.chaintorquenative.mobile.ui.viewmodels.UserProfileViewModel
import com.example.chaintorquenative.mobile.ui.viewmodels.WalletViewModel
import com.example.chaintorquenative.ui.components.common.EmptyState
import com.example.chaintorquenative.ui.components.common.LoadingState
import com.example.chaintorquenative.ui.screens.profile.components.ConnectWalletPrompt
import com.example.chaintorquenative.ui.screens.profile.components.ProfileHeader
import com.example.chaintorquenative.ui.screens.profile.components.PurchasedItemsGrid
import com.example.chaintorquenative.ui.theme.AppColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    onNavigateToWallet: () -> Unit = {},
    viewModel: UserProfileViewModel = hiltViewModel(),
    walletViewModel: WalletViewModel = hiltViewModel(),
    onNavigateToModelViewer: (modelUrl: String, title: String) -> Unit = { _, _ -> }
) {
    val walletAddress  by walletViewModel.walletAddress.observeAsState()
    val isConnected    by walletViewModel.isConnected.observeAsState(false)
    val balance        by walletViewModel.balance.observeAsState("")
    val userPurchases  by viewModel.userPurchases.observeAsState(emptyList())
    val loading        by viewModel.loading.observeAsState(false)
    val isRefreshing   by viewModel.isRefreshing.observeAsState(false)

    // Auto-refresh on resume
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner, walletAddress) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME && walletAddress != null) {
                viewModel.loadUserData(walletAddress!!)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    LaunchedEffect(walletAddress) {
        walletAddress?.let { viewModel.loadUserData(it) }
    }

    androidx.compose.foundation.layout.Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(listOf(AppColors.GradientStart, AppColors.GradientEnd))
            )
    ) {
        if (!isConnected || walletAddress == null) {
            ConnectWalletPrompt(onConnectWallet = onNavigateToWallet)
        } else {
            Column(modifier = Modifier.fillMaxSize()) {
                ProfileHeader(address = walletAddress!!, balance = balance)

                PullToRefreshBox(
                    isRefreshing = isRefreshing,
                    onRefresh = { viewModel.refresh() },
                    modifier = Modifier.weight(1f)
                ) {
                    when {
                        loading && !isRefreshing -> LoadingState()
                        userPurchases.isEmpty()  -> EmptyState("No purchases yet")
                        else -> PurchasedItemsGrid(
                            items = userPurchases,
                            onView3D = onNavigateToModelViewer
                        )
                    }
                }
            }
        }
    }
}
