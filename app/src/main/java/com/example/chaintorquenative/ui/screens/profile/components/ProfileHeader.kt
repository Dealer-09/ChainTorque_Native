package com.example.chaintorquenative.ui.screens.profile.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.chaintorquenative.ui.theme.AppColors

@Composable
fun ProfileHeader(address: String, balance: String) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color.Transparent
    ) {
        Row(
            modifier = Modifier.padding(24.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.linearGradient(listOf(AppColors.Primary, AppColors.Secondary))
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Filled.Person,
                    contentDescription = null,
                    tint = AppColors.OnAccent, // icon sits on the accent-filled avatar
                    modifier = Modifier.size(30.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column {
                Text(
                    text = "My Profile",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = AppColors.OnBg
                )
                Text(
                    text = "${address.take(6)}...${address.takeLast(4)}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = AppColors.OnBg.copy(alpha = 0.7f)
                )
                if (balance.isNotEmpty()) {
                    Text(
                        text = "$balance ETH",
                        style = MaterialTheme.typography.bodySmall,
                        color = AppColors.Success,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}
