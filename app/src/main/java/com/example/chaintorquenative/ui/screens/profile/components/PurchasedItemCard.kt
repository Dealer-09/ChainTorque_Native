package com.example.chaintorquenative.ui.screens.profile.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ViewInAr
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.chaintorquenative.mobile.data.api.MarketplaceItem
import com.example.chaintorquenative.ui.theme.AppColors

// ─── Purchased Items Grid ─────────────────────────────────────────────────────

@Composable
fun PurchasedItemsGrid(
    items: List<MarketplaceItem>,
    onView3D: (modelUrl: String, title: String) -> Unit = { _, _ -> }
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        contentPadding = PaddingValues(16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(items, key = { it.tokenId ?: "" }) { item ->
            PurchasedItemCard(item = item, onView3D = onView3D)
        }
    }
}

// ─── Purchased Item Card ──────────────────────────────────────────────────────

@Composable
fun PurchasedItemCard(
    item: MarketplaceItem,
    onView3D: (modelUrl: String, title: String) -> Unit = { _, _ -> }
) {
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
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = item.title ?: "Untitled",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White,
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
            }
        }
    }
}
