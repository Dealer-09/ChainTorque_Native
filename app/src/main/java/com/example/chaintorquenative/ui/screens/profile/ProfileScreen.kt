package com.example.chaintorquenative.ui.screens.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.chaintorquenative.mobile.ui.viewmodels.UserProfileViewModel
import com.example.chaintorquenative.mobile.ui.viewmodels.WalletViewModel
import com.example.chaintorquenative.ui.components.common.LoadingState
import com.example.chaintorquenative.ui.screens.profile.components.ConnectWalletPrompt
import com.example.chaintorquenative.ui.screens.profile.components.CreatedNFTsGrid
import com.example.chaintorquenative.ui.screens.profile.components.ProfileHeader
import com.example.chaintorquenative.ui.screens.profile.components.PurchasedItemsGrid
import com.example.chaintorquenative.ui.screens.profile.components.SoldItemsGrid
import com.example.chaintorquenative.ui.theme.AppColors

private val tabs = listOf("Purchased", "Created", "Sales")

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
    val userNFTs       by viewModel.userNFTs.observeAsState(emptyList())
    val userSales      by viewModel.userSales.observeAsState(emptyList())
    val loading        by viewModel.loading.observeAsState(false)
    val isRefreshing   by viewModel.isRefreshing.observeAsState(false)

    var selectedTab    by remember { mutableStateOf(0) }

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

    Box(
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

                // ── Profile Header ─────────────────────────────────────────────
                ProfileHeader(address = walletAddress!!, balance = balance)

                // ── Purchased | Created tabs ───────────────────────────────────
                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = Color.Transparent,
                    contentColor = AppColors.Primary,
                    indicator = { tabPositions ->
                        if (selectedTab < tabPositions.size) {
                            TabRowDefaults.SecondaryIndicator(
                                modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                                color = AppColors.Primary
                            )
                        }
                    },
                    divider = { HorizontalDivider(color = Color.White.copy(alpha = 0.08f)) }
                ) {
                    tabs.forEachIndexed { index, title ->
                        Tab(
                            selected = selectedTab == index,
                            onClick  = { selectedTab = index },
                            text = {
                                Text(
                                    text = title,
                                    fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal,
                                    color = if (selectedTab == index) AppColors.Primary else Color.White.copy(alpha = 0.5f)
                                )
                            }
                        )
                    }
                }

                // ── Content with pull-to-refresh ───────────────────────────────
                PullToRefreshBox(
                    isRefreshing = isRefreshing,
                    onRefresh    = { viewModel.refresh() },
                    modifier     = Modifier.weight(1f)
                ) {
                    if (loading && !isRefreshing) {
                        LoadingState()
                    } else {
                        when (selectedTab) {
                            0 -> PurchasedItemsGrid(
                                items   = userPurchases,
                                onView3D = onNavigateToModelViewer
                            )
                            1 -> CreatedNFTsGrid(
                                items   = userNFTs,
                                onView3D = onNavigateToModelViewer
                            )
                            2 -> SoldItemsGrid(items = userSales)
                        }
                    }
                }
            }
        }
    }
}
