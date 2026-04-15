package com.example.chaintorquenative.ui.screens.settings

import androidx.compose.foundation.background
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.chaintorquenative.ui.theme.AppColors

@Composable
fun SettingsScreen() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(listOf(AppColors.GradientStart, AppColors.GradientEnd))
            )
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

            SettingsSection(title = "About") {
                SettingsItem(icon = Icons.Filled.Info,  title = "App Version", subtitle = "1.0.0")
                SettingsItem(icon = Icons.Filled.Code,  title = "Network",     subtitle = "Ethereum Sepolia Testnet")
                SettingsItem(icon = Icons.Filled.Cloud, title = "Backend",     subtitle = "chaintorque-backend.onrender.com")
            }

            Spacer(modifier = Modifier.height(16.dp))

            SettingsSection(title = "Links") {
                SettingsItem(icon = Icons.Filled.Language,    title = "Website",          subtitle = "chaintorque.com")
                SettingsItem(icon = Icons.Filled.Description, title = "Terms of Service", subtitle = "Read our terms")
                SettingsItem(icon = Icons.Filled.PrivacyTip,  title = "Privacy Policy",   subtitle = "How we handle your data")
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
            Column(modifier = Modifier.padding(8.dp)) {
                content()
            }
        }
    }
}

// ─── Settings Row ─────────────────────────────────────────────────────────────

@Composable
private fun SettingsItem(icon: ImageVector, title: String, subtitle: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = Color.White.copy(alpha = 0.7f), modifier = Modifier.size(24.dp))
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title,    style = MaterialTheme.typography.bodyLarge,  fontWeight = FontWeight.Medium, color = Color.White)
            Text(text = subtitle, style = MaterialTheme.typography.bodySmall, color = Color.White.copy(alpha = 0.6f))
        }
        Icon(Icons.Filled.ChevronRight, contentDescription = null, tint = Color.White.copy(alpha = 0.3f))
    }
}
