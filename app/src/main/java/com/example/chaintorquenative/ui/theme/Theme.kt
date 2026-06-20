package com.example.chaintorquenative.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color

// Minimalist black & white. The Material color scheme is derived from the same
// monochrome palette as AppColors, so built-in components (TopAppBar, Surface,
// Switch, Button content color, …) invert correctly between dark and light.
// onPrimary is the inverse of primary, so filled accent buttons never render
// same-on-same (e.g. white button → black label in dark mode).

private fun darkScheme() = darkColorScheme(
    primary       = Color(0xFFFFFFFF),
    onPrimary     = Color(0xFF000000),
    secondary     = Color(0xFFFFFFFF),
    onSecondary   = Color(0xFF000000),
    background    = Color(0xFF000000),
    onBackground  = Color(0xFFFFFFFF),
    surface       = Color(0xFF141414),
    onSurface     = Color(0xFFFFFFFF),
    outline       = Color(0xFF262626),
    error         = Color(0xFFFFFFFF),
    onError       = Color(0xFF000000),
)

private fun lightScheme() = lightColorScheme(
    primary       = Color(0xFF000000),
    onPrimary     = Color(0xFFFFFFFF),
    secondary     = Color(0xFF000000),
    onSecondary   = Color(0xFFFFFFFF),
    background    = Color(0xFFFFFFFF),
    onBackground  = Color(0xFF000000),
    surface       = Color(0xFFFFFFFF),
    onSurface     = Color(0xFF000000),
    outline       = Color(0xFFE2E2E2),
    error         = Color(0xFF000000),
    onError       = Color(0xFFFFFFFF),
)

@Composable
fun ChainTorqueTheme(
    darkTheme: Boolean = true, // dark by default; toggled via ThemeManager
    content: @Composable () -> Unit
) {
    // Keep the custom AppColors palette in lock-step with the Material scheme.
    SideEffect { AppColors.setDark(darkTheme) }

    MaterialTheme(
        colorScheme = if (darkTheme) darkScheme() else lightScheme(),
        content = content
    )
}
