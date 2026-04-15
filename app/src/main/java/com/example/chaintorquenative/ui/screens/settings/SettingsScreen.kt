package com.example.chaintorquenative.ui.screens.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.chaintorquenative.BuildConfig
import com.example.chaintorquenative.ui.theme.AppColors

@Composable
fun SettingsScreen() {
    val uriHandler = LocalUriHandler.current

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(AppColors.GradientStart, AppColors.GradientEnd)))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Text(
                text = "Settings",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )

            Spacer(modifier = Modifier.height(24.dp))

            // ── About (static info, no chevron) ──────────────────────────────
            SettingsSection(title = "About") {
                SettingsItem(icon = Icons.Filled.Info,  title = "App Version", subtitle = "1.0.0")
                SettingsItem(icon = Icons.Filled.Code,  title = "Network",     subtitle = "Ethereum Sepolia Testnet")
                SettingsItem(
                    icon = Icons.Filled.Cloud,
                    title = "Backend",
                    subtitle = "chaintorque-backend.onrender.com",
                    url = "https://chaintorque-backend.onrender.com",
                    onOpen = { uriHandler.openUri(it) }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // ── Links (clickable, shows chevron) ─────────────────────────────
            SettingsSection(title = "Links") {
                SettingsItem(
                    icon = Icons.Filled.Language,
                    title = "Website",
                    subtitle = "chaintorque.com",
                    url = "https://chaintorque.com",
                    onOpen = { uriHandler.openUri(it) }
                )
                SettingsItem(
                    icon = Icons.Filled.Description,
                    title = "Terms of Service",
                    subtitle = "Read our terms",
                    url = "https://chaintorque.com/terms",
                    onOpen = { uriHandler.openUri(it) }
                )
                SettingsItem(
                    icon = Icons.Filled.PrivacyTip,
                    title = "Privacy Policy",
                    subtitle = "How we handle your data",
                    url = "https://chaintorque.com/privacy",
                    onOpen = { uriHandler.openUri(it) }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // ── Debug (only in debug builds) ──────────────────────────────────
            if (BuildConfig.DEBUG) {
                SettingsSection(title = "Debug") {
                    SettingsItem(
                        icon = Icons.Filled.BugReport,
                        title = "Contract Address",
                        subtitle = "${BuildConfig.CONTRACT_ADDRESS.take(10)}…"
                    )
                }
            }
        }
    }
}

// ─── Section Wrapper ──────────────────────────────────────────────────────────

@Composable
private fun SettingsSection(title: String, content: @Composable () -> Unit) {
    Column {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
            color = AppColors.Primary,
            modifier = Modifier.padding(vertical = 8.dp)
        )
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = AppColors.CardBg)
        ) {
            Column(modifier = Modifier.padding(8.dp)) { content() }
        }
    }
}

// ─── Settings Row ─────────────────────────────────────────────────────────────

@Composable
private fun SettingsItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    url: String? = null,
    onOpen: ((String) -> Unit)? = null
) {
    val isClickable = url != null && onOpen != null
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .then(
                if (isClickable) Modifier.clickable { onOpen!!(url!!) }
                else Modifier
            )
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = Color.White.copy(alpha = 0.7f), modifier = Modifier.size(24.dp))
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title,    style = MaterialTheme.typography.bodyLarge,  fontWeight = FontWeight.Medium, color = Color.White)
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = Color.White.copy(alpha = 0.6f))
        }
        // Only show arrow for tappable rows
        if (isClickable) {
            Icon(Icons.Filled.ChevronRight, contentDescription = null, tint = Color.White.copy(alpha = 0.4f))
        }
    }
}
