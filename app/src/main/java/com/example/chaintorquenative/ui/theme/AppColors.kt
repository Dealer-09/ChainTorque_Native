package com.example.chaintorquenative.ui.theme

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color

// ─── ChainTorque — minimalist black & white theme ────────────────────────────
// Single source of truth for colors. The palette is intentionally monochrome:
// light mode is the exact inverse of dark mode. The palette is held in snapshot
// state so flipping the theme at runtime recomposes every screen that reads an
// AppColors.* value — no per-screen wiring needed.
//
// Existing member names (Primary, CardBg, Success, …) are kept so all call sites
// compile unchanged; in a monochrome theme the semantic ones collapse onto the
// foreground/muted tones (meaning is carried by text + weight, not hue).

data class Palette(
    val bg: Color,          // app background
    val bgElevated: Color,  // subtle elevated background
    val card: Color,        // cards / sheets
    val border: Color,      // hairline dividers / outlines
    val accent: Color,      // filled buttons / active state (the foreground tone)
    val onAccent: Color,    // text / icons sitting on `accent`
    val onBg: Color,        // primary text / icons on `bg`
    val onBgMuted: Color,   // secondary / hint text
)

private val DarkPalette = Palette(
    bg         = Color(0xFF000000),
    bgElevated = Color(0xFF0D0D0D),
    card       = Color(0xFF141414),
    border     = Color(0xFF262626),
    accent     = Color(0xFFFFFFFF),
    onAccent   = Color(0xFF000000),
    onBg       = Color(0xFFFFFFFF),
    onBgMuted  = Color(0xFF8A8A8A),
)

// Exact inverse of the dark palette.
private val LightPalette = Palette(
    bg         = Color(0xFFFFFFFF),
    bgElevated = Color(0xFFF5F5F5),
    card       = Color(0xFFFFFFFF),
    border     = Color(0xFFE2E2E2),
    accent     = Color(0xFF000000),
    onAccent   = Color(0xFFFFFFFF),
    onBg       = Color(0xFF000000),
    onBgMuted  = Color(0xFF6B6B6B),
)

object AppColors {
    // Backed by snapshot state: readers in @Composable scopes recompose on flip.
    internal var palette by mutableStateOf(DarkPalette)
    var isDark: Boolean = true
        private set

    fun setDark(dark: Boolean) {
        isDark = dark
        palette = if (dark) DarkPalette else LightPalette
    }

    // ── Legacy names (kept so existing call sites compile unchanged) ──────────
    val Primary       get() = palette.accent
    val Secondary     get() = palette.onBgMuted
    val GradientStart get() = palette.bgElevated
    val GradientEnd   get() = palette.bg
    val CardBg        get() = palette.card
    val Success       get() = palette.onBg       // monochrome: no hue
    val Warning       get() = palette.onBgMuted
    val Error         get() = palette.onBg

    // ── Semantic accessors (use these going forward) ─────────────────────────
    val OnBg       get() = palette.onBg
    val OnBgMuted  get() = palette.onBgMuted
    val OnAccent   get() = palette.onAccent
    val Border     get() = palette.border
}
