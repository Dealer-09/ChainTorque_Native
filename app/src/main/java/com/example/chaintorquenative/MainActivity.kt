package com.example.chaintorquenative

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.chaintorquenative.ui.components.BottomNavigationBar
import com.example.chaintorquenative.ui.screens.MarketplaceScreen // This is your Composable Marketplace
import com.example.chaintorquenative.ui.screens.ProfileScreen // This is your Composable Profile
import com.example.chaintorquenative.ui.screens.WalletScreen
import com.example.chaintorquenative.ui.screens.SettingsScreen
import com.example.chaintorquenative.ui.theme.ChainTorqueTheme
import com.example.chaintorquenative.ui.viewmodels.MainViewModel
import com.example.chaintorquenative.ui.viewmodels.WalletViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    // private val walletViewModel: WalletViewModel by viewModels() // Not used directly here for nav

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            ChainTorqueTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    ChainTorqueApp()
                }
            }
        }
    }
}

@Composable
fun ChainTorqueApp() {
    val navController = rememberNavController()
    val mainViewModel: MainViewModel = hiltViewModel()
    val currentScreen by mainViewModel.currentScreen.observeAsState(MainViewModel.Screen.MARKETPLACE)

    mainViewModel.navigateToWalletRequest.observeAsState().value?.getContentIfNotHandled()?.let {
        // When triggered, update the current screen state and navigate using Compose NavController
        mainViewModel.navigateToScreen(MainViewModel.Screen.WALLET)
        navController.navigate("wallet") {
            // Optional: Configure navigation behavior, e.g., launchSingleTop
            launchSingleTop = true
        }
    }

    Scaffold(
        bottomBar = {
            BottomNavigationBar(
                currentScreen = currentScreen,
                onScreenSelected = { screen ->
                    mainViewModel.navigateToScreen(screen)
                    val route = when (screen) {
                        MainViewModel.Screen.MARKETPLACE -> "marketplace"
                        MainViewModel.Screen.PROFILE -> "profile"
                        MainViewModel.Screen.WALLET -> "wallet"
                        MainViewModel.Screen.SETTINGS -> "settings"
                    }
                    navController.navigate(route) {
                        // Pop up to the start destination of the graph to
                        // avoid building up a large stack of destinations
                        // on the back stack as users select items
                        popUpTo(navController.graph.startDestinationId) {
                            saveState = true
                        }
                        // Avoid multiple copies of the same destination when
                        // reselecting the same item
                        launchSingleTop = true
                        // Restore state when reselecting a previously selected item
                        restoreState = true
                    }
                }
            )
        }
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = "marketplace", // Assuming "marketplace" is a Composable screen
            modifier = Modifier.padding(paddingValues)
        ) {
            // IMPORTANT: These routes are for your COMPOSABLE screens.
            // If "marketplace" or "profile" should actually display the traditional Fragments,
            // this NavHost setup needs to change significantly (e.g., using AndroidView to host Fragments).
            // For this fix, I am assuming that "marketplace" and "profile" are Composable screens,
            // and the Fragments are separate UI pieces that just need to trigger navigation.

            composable("marketplace") {
                MarketplaceScreen( // Composable screen
                    onNavigateToWallet = { // Navigation from within the Composable screen
                        mainViewModel.navigateToScreen(MainViewModel.Screen.WALLET)
                        navController.navigate("wallet")
                    }
                )
            }
            composable("profile") {
                ProfileScreen( // Composable screen
                    onNavigateToWallet = { // Navigation from within the Composable screen
                        mainViewModel.navigateToScreen(MainViewModel.Screen.WALLET)
                        navController.navigate("wallet")
                    }
                )
            }
            composable("wallet") {
                WalletScreen()
            }
            composable("settings") {
                SettingsScreen()
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ChainTorqueAppPreview() {
    ChainTorqueTheme {
        ChainTorqueApp()
    }
}

