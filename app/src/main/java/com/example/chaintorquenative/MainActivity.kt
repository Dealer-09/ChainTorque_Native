package com.example.chaintorquenative

import android.content.Intent
import android.os.Bundle
import android.util.Log
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
import com.example.chaintorquenative.ui.screens.AnimatedSplashScreen
import com.example.chaintorquenative.ui.theme.ChainTorqueTheme
import com.reown.appkit.client.AppKit
import dagger.hilt.android.AndroidEntryPoint

private const val TAG = "MainActivity"

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Handle deep link if app was launched from WalletConnect
        handleIntent(intent)

        setContent {
            ChainTorqueTheme {
                ChainTorqueAppWithSplash()
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        Log.d(TAG, "onNewIntent received: ${intent.data}")
        handleIntent(intent)
    }

    private fun handleIntent(intent: Intent?) {
        intent?.data?.let { uri ->
            Log.d(TAG, "Deep link URI: $uri")

            // Check if this is a WalletConnect callback
            if (uri.scheme == "chaintorque" && uri.host == "walletconnect") {
                Log.d(TAG, "WalletConnect callback received!")

                // The AppKit SDK automatically handles the session approval
                // via its internal relay connection. The deep link just brings
                // the app back to the foreground.

                // Log the URI for debugging
                val wcUri = uri.getQueryParameter("uri")
                if (wcUri != null) {
                    Log.d(TAG, "WC URI in callback: $wcUri")
                }
                // No need to call handleDeepLink - relay handles session automatically
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