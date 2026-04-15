package com.example.chaintorquenative.ui.screens.marketplace.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Favorite
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.chaintorquenative.mobile.data.api.MarketplaceItem
import com.example.chaintorquenative.ui.theme.AppColors

// ─── Item Detail Bottom Sheet ─────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ItemDetailSheet(
    item: MarketplaceItem,
    walletAddress: String?,
    onDismiss: () -> Unit,
    onPurchase: (MarketplaceItem) -> Unit,
    onConnectWallet: () -> Unit,
    onView3D: (modelUrl: String, title: String) -> Unit = { _, _ -> }
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = AppColors.CardBg,
        contentColor = Color.White
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
        ) {
            // Thumbnail
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .padding(bottom = 16.dp)
            ) {
                AsyncImage(
                    model = item.getDisplayImage(),
                    contentDescription = item.title,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }

            // Title
            Text(
                text = item.title ?: "Untitled",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Seller
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Filled.Person,
                    contentDescription = null,
                    tint = Color.White.copy(alpha = 0.7f),
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "by ${item.getShortSeller()}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.7f)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Description
            Text(
                text = item.description ?: "No description available",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.8f)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Stats
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StatItem(icon = Icons.Outlined.Visibility, value = "${item.views ?: 0}", label = "Views")
                StatItem(icon = Icons.Outlined.Favorite,   value = "${item.likes ?: 0}", label = "Likes")
                StatItem(icon = Icons.Filled.Category,     value = item.category ?: "General", label = "Category")
            }

            Spacer(modifier = Modifier.height(16.dp))

            // View 3D button
            if (!item.modelUrl.isNullOrBlank()) {
                OutlinedButton(
                    onClick = { onView3D(item.modelUrl!!, item.title ?: "3D Model") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = AppColors.Primary),
                    border = androidx.compose.foundation.BorderStroke(
                        width = 1.dp,
                        color = AppColors.Primary.copy(alpha = 0.6f)
                    )
                ) {
                    Icon(Icons.Filled.ViewInAr, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("View 3D Model", fontWeight = FontWeight.SemiBold)
                }
                Spacer(modifier = Modifier.height(12.dp))
            }

            // Price + Buy
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Price",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.6f)
                    )
                    Text(
                        text = "${item.getDisplayPrice()} ETH",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = AppColors.Primary
                    )
                }

                Button(
                    onClick = {
                        if (walletAddress != null) onPurchase(item) else onConnectWallet()
                    },
                    modifier = Modifier.height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (walletAddress != null) AppColors.Primary else AppColors.Success
                    )
                ) {
                    Icon(
                        if (walletAddress != null) Icons.Filled.ShoppingCart else Icons.Filled.AccountBalanceWallet,
                        contentDescription = null
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (walletAddress != null) "Buy Now" else "Connect Wallet",
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

// ─── Stat Item ────────────────────────────────────────────────────────────────

@Composable
fun StatItem(
    icon: ImageVector,
    value: String,
    label: String
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(
            icon,
            contentDescription = null,
            tint = Color.White.copy(alpha = 0.6f),
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = Color.White.copy(alpha = 0.5f)
        )
    }
}
