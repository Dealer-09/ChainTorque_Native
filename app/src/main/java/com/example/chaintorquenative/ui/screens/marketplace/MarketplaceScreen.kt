package com.example.chaintorquenative.ui.screens.marketplace

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalFocusManager
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.compose.ui.platform.LocalLifecycleOwner
import com.example.chaintorquenative.mobile.data.api.MarketplaceItem
import com.example.chaintorquenative.mobile.ui.viewmodels.MarketplaceViewModel
import com.example.chaintorquenative.mobile.ui.viewmodels.WalletViewModel
import com.example.chaintorquenative.ui.components.common.EmptyState
import com.example.chaintorquenative.ui.components.common.ErrorState
import com.example.chaintorquenative.ui.components.common.LoadingState
import com.example.chaintorquenative.ui.screens.marketplace.components.*
import com.example.chaintorquenative.ui.theme.AppColors

// ─── Categories ───────────────────────────────────────────────────────────────
private val categories = listOf(
    "All" to "🏠",
    "Mechanical" to "⚙️",
    "Automotive" to "🚗",
    "Aerospace" to "✈️",
    "Robotics" to "🤖",
    "Architecture" to "🏛️",
    "Electronics" to "💡"
)

// ─── Marketplace Screen ───────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MarketplaceScreen(
    onNavigateToWallet: () -> Unit = {},
    viewModel: MarketplaceViewModel = hiltViewModel(),
    walletViewModel: WalletViewModel = hiltViewModel(),
    onNavigateToModelViewer: (modelUrl: String, title: String) -> Unit = { _, _ -> }
) {
    val items          by viewModel.filteredItems.observeAsState(emptyList())
    val loading        by viewModel.loading.observeAsState(false)
    val error          by viewModel.error.observeAsState()
    val searchQuery    by viewModel.searchQuery.observeAsState("")
    val walletAddress  by walletViewModel.walletAddress.observeAsState()
    val selectedItem   by viewModel.selectedItem.observeAsState()
    val isRefreshing   by viewModel.isRefreshing.observeAsState(false)
    val selectedCategory by viewModel.selectedCategory.observeAsState("All")
    var showItemDetail by remember { mutableStateOf(false) }

    val focusManager = LocalFocusManager.current

    // Auto-refresh on ON_RESUME
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) viewModel.loadMarketplaceItems()
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    androidx.compose.foundation.layout.Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(listOf(AppColors.GradientStart, AppColors.GradientEnd))
            )
    ) {
        androidx.compose.foundation.layout.Column(modifier = Modifier.fillMaxSize()) {
            MarketplaceHeader()

            MarketplaceSearchBar(
                query = searchQuery,
                onQueryChange = { viewModel.searchItems(it) },
                onSearch = { focusManager.clearFocus() }
            )

            CategoryChips(
                categories = categories,
                selectedCategory = selectedCategory,
                onCategorySelected = { viewModel.selectCategory(it) }
            )

            PullToRefreshBox(
                isRefreshing = isRefreshing,
                onRefresh = { viewModel.refresh() },
                modifier = Modifier.weight(1f)
            ) {
                when {
                    loading && items.isEmpty() -> LoadingState()
                    error != null              -> ErrorState(
                        message = error ?: "Unknown error",
                        onRetry = { viewModel.loadMarketplaceItems() }
                    )
                    items.isEmpty()            -> EmptyState(message = "No items found")
                    else                       -> MarketplaceGrid(
                        items = items,
                        onItemClick = { item ->
                            viewModel.selectItem(item)
                            showItemDetail = true
                        }
                    )
                }
            }
        }

        // Item detail sheet
        if (showItemDetail && selectedItem != null) {
            ItemDetailSheet(
                item = selectedItem!!,
                walletAddress = walletAddress,
                onDismiss = { showItemDetail = false },
                onPurchase = { item ->
                    if (walletAddress != null) {
                        viewModel.purchaseItem(
                            item.tokenId?.toIntOrNull() ?: 0,
                            walletAddress!!,
                            item.getDisplayPrice()
                        )
                        showItemDetail = false
                    } else {
                        onNavigateToWallet()
                    }
                },
                onConnectWallet = onNavigateToWallet,
                onView3D = { modelUrl, title ->
                    showItemDetail = false
                    onNavigateToModelViewer(modelUrl, title)
                }
            )
        }
    }
}
