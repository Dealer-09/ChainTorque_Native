package com.example.chaintorquenative.ui.screens.profile.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Sell
import androidx.compose.material.icons.filled.ViewInAr
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.chaintorquenative.mobile.data.api.MarketplaceItem
import com.example.chaintorquenative.ui.theme.AppColors

// ─── Purchased Items Grid ─────────────────────────────────────────────────────

@Composable
fun PurchasedItemsGrid(
    items: List<MarketplaceItem>,
    onView3D: (modelUrl: String, title: String) -> Unit = { _, _ -> },
    onResell: (tokenId: Int, newPriceEth: Double) -> Unit = { _, _ -> }
) {
    if (items.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("🛒", style = MaterialTheme.typography.displayMedium)
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "No purchases yet",
                    style = MaterialTheme.typography.bodyLarge,
                    color = AppColors.OnBg.copy(alpha = 0.5f)
                )
                Text(
                    text = "Items you buy will appear here",
                    style = MaterialTheme.typography.bodySmall,
                    color = AppColors.OnBg.copy(alpha = 0.35f)
                )
            }
        }
    } else {
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            contentPadding = PaddingValues(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(items, key = { it.tokenId ?: "" }) { item ->
                PurchasedItemCard(item = item, onView3D = onView3D, onResell = onResell)
            }
        }
    }
}


// ─── Purchased Item Card ──────────────────────────────────────────────────────

@Composable
fun PurchasedItemCard(
    item: MarketplaceItem,
    onView3D: (modelUrl: String, title: String) -> Unit = { _, _ -> },
    onResell: (tokenId: Int, newPriceEth: Double) -> Unit = { _, _ -> }
) {
    var showResell by remember { mutableStateOf(false) }
    var priceInput by remember { mutableStateOf("") }
    val tokenIdInt = item.tokenId?.toIntOrNull()

    if (showResell && tokenIdInt != null) {
        val parsedPrice = priceInput.toDoubleOrNull()
        AlertDialog(
            onDismissRequest = { showResell = false },
            containerColor = AppColors.CardBg,
            titleContentColor = AppColors.OnBg,
            textContentColor = AppColors.OnBg,
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.Sell, contentDescription = null, tint = AppColors.Primary, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Resell NFT", fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = item.title ?: "Untitled",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = AppColors.OnBg
                    )
                    OutlinedTextField(
                        value = priceInput,
                        onValueChange = { new -> priceInput = new.filter { it.isDigit() || it == '.' } },
                        label = { Text("Sale price (ETH)") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = AppColors.OnBg,
                            unfocusedTextColor = AppColors.OnBg,
                            focusedBorderColor = AppColors.Primary,
                            unfocusedBorderColor = AppColors.OnBg.copy(alpha = 0.3f),
                            cursorColor = AppColors.Primary,
                            focusedLabelColor = AppColors.Primary,
                            unfocusedLabelColor = AppColors.OnBg.copy(alpha = 0.6f)
                        )
                    )
                    Text(
                        text = "A 0.00025 ETH listing fee applies. Your wallet will open to sign.",
                        style = MaterialTheme.typography.bodySmall,
                        color = AppColors.OnBg.copy(alpha = 0.5f)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showResell = false
                        onResell(tokenIdInt, parsedPrice ?: 0.0)
                    },
                    enabled = parsedPrice != null && parsedPrice > 0.0,
                    colors = ButtonDefaults.buttonColors(containerColor = AppColors.Primary),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Filled.Sell, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("List for Sale", fontWeight = FontWeight.SemiBold)
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { showResell = false },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = AppColors.OnBg),
                    border = androidx.compose.foundation.BorderStroke(1.dp, AppColors.OnBg.copy(alpha = 0.3f))
                ) { Text("Cancel") }
            }
        )
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = AppColors.CardBg)
    ) {
        Column {
            Box {
                AsyncImage(
                    model = item.getDisplayImage(),
                    contentDescription = item.title,
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1f),
                    contentScale = ContentScale.Crop
                )

                Surface(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp),
                    shape = RoundedCornerShape(8.dp),
                    color = AppColors.Success
                ) {
                    Text(
                        text = "Owned",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = AppColors.OnAccent, // text sits on the filled Success badge
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = item.title ?: "Untitled",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = AppColors.OnBg,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(8.dp))

                if (!item.modelUrl.isNullOrBlank()) {
                    Button(
                        onClick = { onView3D(item.modelUrl!!, item.title ?: "3D Model") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = AppColors.Primary),
                        contentPadding = PaddingValues(8.dp)
                    ) {
                        Icon(
                            Icons.Filled.ViewInAr,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("View 3D", style = MaterialTheme.typography.labelMedium)
                    }
                } else {
                    OutlinedButton(
                        onClick = {},
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        enabled = false,
                        contentPadding = PaddingValues(8.dp)
                    ) {
                        Text("No Model", style = MaterialTheme.typography.labelMedium)
                    }
                }

                // Resell — relist this owned NFT back onto the marketplace
                if (tokenIdInt != null) {
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedButton(
                        onClick = { showResell = true },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = AppColors.Primary),
                        border = androidx.compose.foundation.BorderStroke(1.dp, AppColors.Primary.copy(alpha = 0.6f)),
                        contentPadding = PaddingValues(8.dp)
                    ) {
                        Icon(Icons.Filled.Sell, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Resell", style = MaterialTheme.typography.labelMedium)
                    }
                }
            }
        }
    }
}
