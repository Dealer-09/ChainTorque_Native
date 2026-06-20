package com.example.chaintorquenative.ui.theme

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

// Persisted dark/light preference. Snapshot-state backed so toggling it from the
// Settings screen recomposes the whole app (MainActivity reads `isDark` inside
// setContent). Persistence is plain SharedPreferences — no extra dependency.
//
// Only two modes by design: Dark (default) and Light. There is no "system" mode.
object ThemeManager {
    private const val PREFS = "ct_prefs"
    private const val KEY_DARK = "theme_dark"

    var isDark by mutableStateOf(true)
        private set

    /** Load the saved preference once at app startup (call from MainActivity.onCreate). */
    fun load(context: Context) {
        isDark = prefs(context).getBoolean(KEY_DARK, true)
        AppColors.setDark(isDark)
    }

    /** Toggle + persist. Recomposes the app via the snapshot-state read in setContent. */
    fun setDark(context: Context, dark: Boolean) {
        isDark = dark
        AppColors.setDark(dark)
        prefs(context).edit().putBoolean(KEY_DARK, dark).apply()
    }

    private fun prefs(context: Context) =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
}
