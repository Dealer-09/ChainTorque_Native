@file:OptIn(
    androidx.compose.material.ExperimentalMaterialApi::class,
    com.google.accompanist.navigation.material.ExperimentalMaterialNavigationApi::class
)
@file:Suppress("DEPRECATION") // Accompanist BottomSheetNavigator: cannot migrate until Reown AppKit ships AndroidX nav-material support

package com.example.chaintorquenative

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ModalBottomSheetValue
import androidx.compose.material.rememberModalBottomSheetState
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.lifecycleScope
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.chaintorquenative.mobile.data.repository.ConfigRepository
import com.example.chaintorquenative.mobile.ui.viewmodels.WalletViewModel
import com.example.chaintorquenative.ui.components.BottomNavigationBar
import com.example.chaintorquenative.ui.screens.marketplace.MarketplaceScreen
import com.example.chaintorquenative.ui.screens.ModelViewerScreen
import com.example.chaintorquenative.ui.screens.profile.ProfileScreen
import com.example.chaintorquenative.ui.screens.wallet.WalletScreen
import com.example.chaintorquenative.ui.screens.settings.SettingsScreen
import com.example.chaintorquenative.ui.screens.AnimatedSplashScreen
import com.example.chaintorquenative.ui.theme.ChainTorqueTheme
import com.example.chaintorquenative.ui.theme.ThemeManager
import com.google.accompanist.navigation.material.BottomSheetNavigator
import com.google.accompanist.navigation.material.ExperimentalMaterialNavigationApi
import com.google.accompanist.navigation.material.ModalBottomSheetLayout
import com.reown.appkit.client.AppKit
import com.reown.appkit.ui.appKitGraph
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val TAG = "MainActivity"

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject lateinit var configRepository: ConfigRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Handle WalletConnect deep link on launch
        handleWalletConnectDeepLink(intent)

        // Fetch runtime config (contract address etc.) so a contract redeploy is
        // picked up without an app rebuild. Non-blocking; falls back to BuildConfig.
        lifecycleScope.launch { configRepository.loadConfig() }

        // Load the saved dark/light preference before first composition.
        ThemeManager.load(this)

        setContent {
            // Reading ThemeManager.isDark (snapshot state) here means toggling it
            // from Settings recomposes the whole app into the other theme.
            ChainTorqueTheme(darkTheme = ThemeManager.isDark) {
                ChainTorqueAppWithSplash()
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        // Handle WalletConnect deep link when app is already running
        handleWalletConnectDeepLink(intent)
    }

    private fun handleWalletConnectDeepLink(intent: Intent?) {
        val data = intent?.dataString ?: return
        if (data.contains("wc_ev") || data.contains("wc?")) {
            Log.d(TAG, "Handling WalletConnect deep link: $data")
            AppKit.handleDeepLink(data) { error ->
                lifecycleScope.launch(Dispatchers.Main) {
                    Log.e(TAG, "WC deep link error: ${error.throwable.message}")
                    Toast.makeText(
                        this@MainActivity,
                        "Wallet connection error: ${error.throwable.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }
}

// Screen enum for navigation
enum class Screen {
    MARKETPLACE, PROFILE, WALLET, SETTINGS
}

@Composable
fun ChainTorqueAppWithSplash() {
    var showSplash by remember { mutableStateOf(true) }

    if (showSplash) {
        AnimatedSplashScreen(
            onSplashComplete = {
                showSplash = false
            }
        )
    } else {
        ChainTorqueApp()
    }
}

@Composable
fun ChainTorqueApp() {
    // Accompanist bottom sheet state for AppKit modal
    val sheetState = rememberModalBottomSheetState(
        initialValue = ModalBottomSheetValue.Hidden,
        skipHalfExpanded = true
    )
    val bottomSheetNavigator = BottomSheetNavigator(sheetState)
    val navController = rememberNavController(bottomSheetNavigator)
    var currentScreen by remember { mutableStateOf(Screen.MARKETPLACE) }

    // Create a SHARED WalletViewModel at the app level
    val sharedWalletViewModel: WalletViewModel = hiltViewModel()

    ModalBottomSheetLayout(bottomSheetNavigator = bottomSheetNavigator) {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            bottomBar = {
                BottomNavigationBar(
                    currentScreen = currentScreen,
                    onScreenSelected = { screen ->
                        currentScreen = screen
                        val route = when (screen) {
                            Screen.MARKETPLACE -> "marketplace"
                            Screen.PROFILE -> "profile"
                            Screen.WALLET -> "wallet"
                            Screen.SETTINGS -> "settings"
                        }
                        navController.navigate(route) {
                            popUpTo(navController.graph.startDestinationId) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            }
        ) { paddingValues ->
            NavHost(
                navController = navController,
                startDestination = "marketplace",
                modifier = Modifier.padding(paddingValues)
            ) {
                composable("marketplace") {
                    MarketplaceScreen(
                        onNavigateToWallet = {
                            currentScreen = Screen.WALLET
                            navController.navigate("wallet")
                        },
                        walletViewModel = sharedWalletViewModel,
                        onNavigateToModelViewer = { modelUrl, title ->
                            navController.navigate(
                                "model_viewer/${Uri.encode(modelUrl)}?title=${Uri.encode(title)}"
                            )
                        }
                    )
                }
                composable("profile") {
                    ProfileScreen(
                        onNavigateToWallet = {
                            currentScreen = Screen.WALLET
                            navController.navigate("wallet")
                        },
                        walletViewModel = sharedWalletViewModel,
                        onNavigateToModelViewer = { modelUrl, title ->
                            navController.navigate(
                                "model_viewer/${Uri.encode(modelUrl)}?title=${Uri.encode(title)}"
                            )
                        }
                    )
                }
                composable("wallet") {
                    WalletScreen(
                        viewModel = sharedWalletViewModel,
                        navController = navController
                    )
                }
                composable("settings") {
                    SettingsScreen()
                }
                composable("model_viewer/{modelUrl}?title={title}") { backStackEntry ->
                    val modelUrl = Uri.decode(backStackEntry.arguments?.getString("modelUrl") ?: "")
                    val title = Uri.decode(backStackEntry.arguments?.getString("title") ?: "3D Model")
                    ModelViewerScreen(
                        modelUrl = modelUrl,
                        title = title,
                        onBack = { navController.popBackStack() }
                    )
                }
                // AppKit modal routes (required for WalletConnect QR code / wallet selection)
                appKitGraph(navController)
            }
        }
    }
}
