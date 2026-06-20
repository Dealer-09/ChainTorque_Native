package com.example.chaintorquenative.ui.screens.profile.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.chaintorquenative.mobile.data.api.TransactionRecord
import com.example.chaintorquenative.ui.theme.AppColors

// ─── Activity / Transaction History List ──────────────────────────────────────

@Composable
fun ActivityList(transactions: List<TransactionRecord>) {
    if (transactions.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("📜", style = MaterialTheme.typography.displayMedium)
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "No activity yet",
                    style = MaterialTheme.typography.bodyLarge,
                    color = AppColors.OnBg.copy(alpha = 0.5f)
                )
                Text(
                    text = "Mints, purchases, sales and relists appear here",
                    style = MaterialTheme.typography.bodySmall,
                    color = AppColors.OnBg.copy(alpha = 0.35f)
                )
            }
        }
    } else {
        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(transactions, key = { it.transactionHash ?: "${it.tokenId}-${it.type}-${it.createdAt}" }) { tx ->
                TransactionRow(tx)
            }
        }
    }
}

@Composable
private fun TransactionRow(tx: TransactionRecord) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = AppColors.CardBg)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Type icon
            Box(
                modifier = Modifier.size(40.dp),
                contentAlignment = Alignment.Center
            ) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = AppColors.Primary.copy(alpha = 0.12f),
                    modifier = Modifier.fillMaxSize()
                ) {}
                Icon(
                    imageVector = iconFor(tx.type),
                    contentDescription = tx.type,
                    tint = AppColors.Primary,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = labelFor(tx.type),
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = AppColors.OnBg
                )
                Text(
                    text = tx.title ?: tx.tokenId?.let { "Token #$it" } ?: "—",
                    style = MaterialTheme.typography.bodySmall,
                    color = AppColors.OnBg.copy(alpha = 0.6f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                tx.createdAt?.takeIf { it.length >= 10 }?.let { iso ->
                    Text(
                        text = iso.substring(0, 10),
                        style = MaterialTheme.typography.labelSmall,
                        color = AppColors.OnBg.copy(alpha = 0.4f)
                    )
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                if ((tx.price ?: 0.0) > 0.0) {
                    Text(
                        text = "${tx.price} ${tx.currency ?: "ETH"}",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = AppColors.Primary
                    )
                }
                tx.transactionHash?.takeIf { it.length >= 10 }?.let { hash ->
                    Text(
                        text = "${hash.take(6)}…${hash.takeLast(4)}",
                        style = MaterialTheme.typography.labelSmall,
                        color = AppColors.OnBg.copy(alpha = 0.4f)
                    )
                }
            }
        }
    }
}

private fun iconFor(type: String?): ImageVector = when (type?.lowercase()) {
    "mint"     -> Icons.Filled.AddCircle
    "purchase" -> Icons.Filled.ShoppingCart
    "sale"     -> Icons.Filled.AttachMoney
    "relist"   -> Icons.Filled.Sell
    "listing"  -> Icons.Filled.Sell
    "transfer" -> Icons.Filled.SwapHoriz
    else       -> Icons.Filled.Receipt
}

private fun labelFor(type: String?): String = when (type?.lowercase()) {
    "mint"     -> "Minted"
    "purchase" -> "Purchased"
    "sale"     -> "Sold"
    "relist"   -> "Relisted"
    "listing"  -> "Listed"
    "transfer" -> "Transferred"
    else       -> (type ?: "Transaction").replaceFirstChar { it.uppercase() }
}
