

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
import com.example.chaintorquenative.ui.screens.MarketplaceScreen
import com.example.chaintorquenative.ui.screens.ProfileScreen
import com.example.chaintorquenative.ui.screens.WalletScreen
import com.example.chaintorquenative.ui.screens.SettingsScreen
import com.example.chaintorquenative.ui.theme.ChainTorqueTheme
import com.example.chaintorquenative.ui.viewmodels.MainViewModel
import com.example.chaintorquenative.ui.viewmodels.WalletViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val walletViewModel: WalletViewModel by viewModels()

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

    Scaffold(
        bottomBar = {
            BottomNavigationBar(
                currentScreen = currentScreen,
                onScreenSelected = { screen ->
                    mainViewModel.navigateToScreen(screen)
                    when (screen) {
                        MainViewModel.Screen.MARKETPLACE -> navController.navigate("marketplace")
                        MainViewModel.Screen.PROFILE -> navController.navigate("profile")
                        MainViewModel.Screen.WALLET -> navController.navigate("wallet")
                        MainViewModel.Screen.SETTINGS -> navController.navigate("settings")
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
                        mainViewModel.navigateToScreen(MainViewModel.Screen.WALLET)
                        navController.navigate("wallet")
                    }
                )
            }
            composable("profile") {
                ProfileScreen(
                    onNavigateToWallet = {
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
