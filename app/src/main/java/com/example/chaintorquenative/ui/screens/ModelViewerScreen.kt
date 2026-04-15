package com.example.chaintorquenative.ui.screens

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ViewInAr
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.chaintorquenative.ui.theme.AppColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModelViewerScreen(
    modelUrl: String,
    title: String = "3D Model",
    onBack: () -> Unit
) {
    val context      = LocalContext.current
    val sanitizedUrl = modelUrl.trim()
    val hasModel     = sanitizedUrl.isNotBlank()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(AppColors.GradientStart, AppColors.GradientEnd)))
    ) {
        Column(modifier = Modifier.fillMaxSize()) {

            // ── Top App Bar ──────────────────────────────────────────────────
            TopAppBar(
                title = {
                    Column {
                        Text(title, fontWeight = FontWeight.SemiBold, color = Color.White, maxLines = 1)
                        Text(
                            text = "Pinch to zoom  •  Drag to rotate",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White.copy(alpha = 0.45f)
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = AppColors.CardBg)
            )

            // ── Viewer Body ──────────────────────────────────────────────────
            Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                when {
                    !hasModel -> NoModelPlaceholder()
                    else -> {
                        Column(
                            modifier = Modifier.fillMaxSize().padding(16.dp),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(Icons.Filled.ViewInAr, contentDescription = "3D Viewer", tint = Color.White, modifier = Modifier.size(64.dp))
                            Spacer(modifier = Modifier.height(16.dp))
                            Text("Ready to View", style = MaterialTheme.typography.titleLarge, color = Color.White)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Launch the 3D viewer to see this model in 3D or AR.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.White.copy(alpha = 0.7f),
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(32.dp))
                            Button(
                                onClick = {
                                    val uri = Uri.parse("https://arvr.google.com/scene-viewer/1.0").buildUpon()
                                        .appendQueryParameter("file", modelUrl)
                                        .appendQueryParameter("mode", "3d_preferred")
                                        .appendQueryParameter("title", title)
                                        .build()
                                    val intent = Intent(Intent.ACTION_VIEW, uri).apply { setPackage("com.google.ar.core") }
                                    try { context.startActivity(intent) }
                                    catch (e: ActivityNotFoundException) {
                                        context.startActivity(Intent(Intent.ACTION_VIEW, uri))
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = AppColors.Primary),
                                modifier = Modifier.fillMaxWidth(0.8f).height(50.dp)
                            ) { Text("Open 3D Viewer", fontWeight = FontWeight.Bold) }
                        }
                    }
                }
            }

            // ── AR + Hint Bar ─────────────────────────────────────────────────
            if (hasModel) {
                Surface(modifier = Modifier.fillMaxWidth(), color = AppColors.CardBg) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Button(
                            onClick = {
                                val uri = Uri.parse(
                                    "https://arvr.google.com/scene-viewer/1.0" +
                                    "?file=${Uri.encode(sanitizedUrl)}" +
                                    "&mode=ar_preferred" +
                                    "&title=${Uri.encode(title)}"
                                )
                                val intent = Intent(Intent.ACTION_VIEW, uri).apply {
                                    setPackage("com.google.android.googlequicksearchbox")
                                }
                                try { context.startActivity(intent) }
                                catch (e: ActivityNotFoundException) {
                                    try { context.startActivity(Intent(Intent.ACTION_VIEW, uri)) }
                                    catch (e2: ActivityNotFoundException) {
                                        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=com.google.ar.core")))
                                    }
                                }
                            },
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 10.dp).height(48.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = AppColors.Primary)
                        ) {
                            Icon(Icons.Filled.ViewInAr, contentDescription = null, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("View in AR", fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            HintChip("👆 Drag", "Rotate")
                            HintChip("🤏 Pinch", "Zoom")
                            HintChip("✌️ Two-finger", "Pan")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun HintChip(gesture: String, action: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(gesture, style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.75f))
        Text(action, style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.4f))
    }
}

@Composable
private fun NoModelPlaceholder() {
    Box(modifier = Modifier.fillMaxSize().background(AppColors.GradientEnd), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(32.dp)) {
            Text("📦", style = MaterialTheme.typography.displayMedium)
            Spacer(modifier = Modifier.height(16.dp))
            Text("No 3D model available", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold, color = Color.White.copy(alpha = 0.8f))
            Spacer(modifier = Modifier.height(8.dp))
            Text("This listing does not have\na 3D model file attached.", style = MaterialTheme.typography.bodyMedium, color = Color.White.copy(alpha = 0.4f), textAlign = TextAlign.Center)
        }
    }
}
