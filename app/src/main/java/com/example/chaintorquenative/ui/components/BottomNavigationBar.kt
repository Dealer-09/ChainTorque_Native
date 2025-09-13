package com.example.chaintorquenative.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.chaintorquenative.ui.viewmodels.MainViewModel

@Composable
fun BottomNavigationBar(
    currentScreen: MainViewModel.Screen,
    onScreenSelected: (MainViewModel.Screen) -> Unit
) {
    NavigationBar {
        val items = listOf(
            BottomNavItem(MainViewModel.Screen.MARKETPLACE, Icons.Default.Home, "Marketplace"),
            BottomNavItem(MainViewModel.Screen.PROFILE, Icons.Default.AccountCircle, "Profile"),
            BottomNavItem(MainViewModel.Screen.WALLET, Icons.Default.AccountBalanceWallet, "Wallet"),
            BottomNavItem(MainViewModel.Screen.SETTINGS, Icons.Default.Settings, "Settings")
        )

        items.forEach { item ->
            NavigationBarItem(
                icon = { Icon(item.icon, contentDescription = item.label) },
                label = { Text(item.label) },
                selected = currentScreen == item.screen,
                onClick = { onScreenSelected(item.screen) }
            )
        }
    }
}

private data class BottomNavItem(
    val screen: MainViewModel.Screen,
    val icon: ImageVector,
    val label: String
)