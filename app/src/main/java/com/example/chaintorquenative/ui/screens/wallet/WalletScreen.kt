package com.example.chaintorquenative.ui.screens.wallet

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LinkOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.chaintorquenative.mobile.ui.viewmodels.WalletViewModel
import com.example.chaintorquenative.ui.theme.AppColors
import com.reown.appkit.ui.openAppKit

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WalletScreen(
    viewModel: WalletViewModel = hiltViewModel(),
    navController: NavController? = null
) {
    val walletAddress    by viewModel.walletAddress.observeAsState()
    val isConnected      by viewModel.isConnected.observeAsState(false)
    val balance          by viewModel.balance.observeAsState("")
    val loading          by viewModel.loading.observeAsState(false)
    val error            by viewModel.error.observeAsState()
    val connectionStatus by viewModel.connectionStatus.observeAsState()

    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(error) {
        error?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearError()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = Color.Transparent
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(listOf(AppColors.GradientStart, AppColors.GradientEnd))
                )
                .padding(padding)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(32.dp))

                // Wallet icon
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .clip(CircleShape)
                        .background(
                            if (isConnected) AppColors.Success.copy(alpha = 0.1f)
                            else AppColors.Primary.copy(alpha = 0.1f)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Filled.AccountBalanceWallet,
                        contentDescription = null,
                        tint = if (isConnected) AppColors.Success else AppColors.Primary,
                        modifier = Modifier.size(50.dp)
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Status text
                Text(
                    text = when (connectionStatus) {
                        WalletViewModel.ConnectionStatus.CONNECTED   -> "Connected"
                        WalletViewModel.ConnectionStatus.CONNECTING  -> "Connecting..."
                        WalletViewModel.ConnectionStatus.ERROR       -> "Connection Error"
                        else                                         -> "Disconnected"
                    },
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = when (connectionStatus) {
                        WalletViewModel.ConnectionStatus.CONNECTED   -> AppColors.Success
                        WalletViewModel.ConnectionStatus.CONNECTING  -> AppColors.Warning
                        WalletViewModel.ConnectionStatus.ERROR       -> Color.Red
                        else                                         -> AppColors.OnBg
                    }
                )

                Spacer(modifier = Modifier.height(32.dp))

                if (!isConnected) {
                    // ── Connect button ────────────────────────────────────────
                    Button(
                        onClick = {
                            viewModel.prepareConnect()
                            navController?.openAppKit(
                                shouldOpenChooseNetwork = false,
                                onError = { /* handled by delegate */ }
                            )
                        },
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3396FF)),
                        enabled = !loading
                    ) {
                        if (loading) {
                            CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White) // on the fixed-blue WalletConnect button
                        } else {
                            Icon(Icons.Filled.AccountBalanceWallet, contentDescription = null, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Connect Wallet", fontWeight = FontWeight.SemiBold)
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "MetaMask, Trust Wallet, Rainbow & more",
                        style = MaterialTheme.typography.bodySmall,
                        color = AppColors.OnBg.copy(alpha = 0.5f)
                    )
                    Spacer(modifier = Modifier.height(24.dp))

                    // Network info card
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = AppColors.CardBg)
                    ) {
                        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(AppColors.Success))
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text("Network", style = MaterialTheme.typography.labelSmall, color = AppColors.OnBg.copy(alpha = 0.6f))
                                Text("Ethereum Sepolia Testnet", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium, color = AppColors.OnBg)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Info card
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = AppColors.CardBg.copy(alpha = 0.5f))
                    ) {
                        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.Top) {
                            Icon(Icons.Filled.Info, contentDescription = null, tint = AppColors.Primary, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = "Connects securely via WalletConnect. Your wallet app will open for approval.",
                                style = MaterialTheme.typography.bodySmall,
                                color = AppColors.OnBg.copy(alpha = 0.7f)
                            )
                        }
                    }
                } else {
                    // ── Connected section ─────────────────────────────────────
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = AppColors.CardBg)
                    ) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            Text("Wallet Address", style = MaterialTheme.typography.labelMedium, color = AppColors.OnBg.copy(alpha = 0.6f))
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(walletAddress ?: "", style = MaterialTheme.typography.bodyMedium, color = AppColors.OnBg, fontWeight = FontWeight.Medium)

                            Spacer(modifier = Modifier.height(16.dp))
                            HorizontalDivider(color = AppColors.OnBg.copy(alpha = 0.1f))
                            Spacer(modifier = Modifier.height(16.dp))

                            Text("Balance", style = MaterialTheme.typography.labelMedium, color = AppColors.OnBg.copy(alpha = 0.6f))
                            Text(
                                text = if (balance.isNotEmpty()) "$balance ETH" else "Loading...",
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold,
                                color = AppColors.Success
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Button(
                        onClick = { viewModel.disconnectWallet() },
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Red.copy(alpha = 0.8f))
                    ) {
                        Icon(Icons.Filled.LinkOff, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Disconnect Wallet", fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }
}
