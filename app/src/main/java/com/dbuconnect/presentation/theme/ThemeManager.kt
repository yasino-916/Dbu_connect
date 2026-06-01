package com.dbuconnect.presentation.theme

import android.content.Context
import android.content.SharedPreferences
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

/**
 * Manages the user's theme preference (System / Light / Dark).
 * Persists the choice in SharedPreferences so it survives app restarts.
 */
object ThemeManager {

    private const val PREFS_NAME = "dbu_connect_theme_prefs"
    private const val KEY_THEME_MODE = "theme_mode"

    // null = follow system, false = force light, true = force dark
    var userDarkModeOverride: Boolean? by mutableStateOf(null)
        private set

    /** Call once from MainActivity.onCreate before setContent. */
    fun init(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        userDarkModeOverride = when (prefs.getString(KEY_THEME_MODE, "system")) {
            "dark" -> true
            "light" -> false
            else -> null
        }
    }

    /** Toggle between dark and light (ignoring system from now on). */
    fun toggleDarkMode(context: Context) {
        val newValue = !(isAppInDarkTheme)
        userDarkModeOverride = newValue
        save(context, if (newValue) "dark" else "light")
    }

    /** Set a specific mode: "system", "light", or "dark". */
    fun setThemeMode(context: Context, mode: String) {
        userDarkModeOverride = when (mode) {
            "dark" -> true
            "light" -> false
            else -> null
        }
        save(context, mode)
    }

    private fun save(context: Context, mode: String) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_THEME_MODE, mode)
            .apply()
    }
}
