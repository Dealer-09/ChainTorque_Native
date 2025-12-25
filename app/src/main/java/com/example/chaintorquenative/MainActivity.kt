package com.example.chaintorquenative

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.chaintorquenative.ui.components.BottomNavigationBar
import com.example.chaintorquenative.ui.screens.MarketplaceScreen
import com.example.chaintorquenative.ui.screens.ProfileScreen
import com.example.chaintorquenative.ui.screens.WalletScreen
import com.example.chaintorquenative.ui.screens.SettingsScreen
import com.example.chaintorquenative.ui.theme.ChainTorqueTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            ChainTorqueTheme {
                ChainTorqueApp()
            }
        }
    }
}

// Screen enum for navigation
enum class Screen {
    MARKETPLACE, PROFILE, WALLET, SETTINGS
}

@Composable
fun ChainTorqueApp() {
    val navController = rememberNavController()
    var currentScreen by remember { mutableStateOf(Screen.MARKETPLACE) }

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
                    }
                )
            }
            composable("profile") {
                ProfileScreen(
                    onNavigateToWallet = {
                        currentScreen = Screen.WALLET
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
