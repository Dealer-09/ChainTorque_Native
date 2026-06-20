package com.example.chaintorquenative.ui.screens.profile.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.chaintorquenative.ui.theme.AppColors

@Composable
fun ConnectWalletPrompt(onConnectWallet: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(100.dp)
                .clip(CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = AppColors.Primary.copy(alpha = 0.1f),
                shape = CircleShape
            ) {}
            Icon(
                Icons.Filled.AccountBalanceWallet,
                contentDescription = null,
                tint = AppColors.Primary,
                modifier = Modifier.size(50.dp)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Connect Your Wallet",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = AppColors.OnBg
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Connect your wallet to view your profile, owned NFTs, and purchase history.",
            style = MaterialTheme.typography.bodyMedium,
            color = AppColors.OnBg.copy(alpha = 0.7f),
            modifier = Modifier.padding(horizontal = 16.dp),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = onConnectWallet,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = AppColors.Primary)
        ) {
            Icon(Icons.Filled.AccountBalanceWallet, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Connect Wallet", fontWeight = FontWeight.SemiBold)
        }
    }
}
