package com.cmptemplate.core.designsystem

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import com.cmptemplate.core.designsystem.settings.AppPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class ThemeMode { SYSTEM, LIGHT, DARK }

/** The user's light/dark choice; follows the device until they pick one. */
class ThemeManager(private val preferences: AppPreferences) {
    private val _mode = MutableStateFlow(
        ThemeMode.entries.firstOrNull { it.name == preferences.getString(KEY) } ?: ThemeMode.SYSTEM,
    )
    val mode: StateFlow<ThemeMode> = _mode.asStateFlow()

    fun setMode(mode: ThemeMode) {
        preferences.putString(KEY, mode.name)
        _mode.value = mode
    }

    private companion object {
        const val KEY = "theme_mode"
    }
}

@Composable
fun ThemeMode.isDark(): Boolean = when (this) {
    ThemeMode.SYSTEM -> isSystemInDarkTheme()
    ThemeMode.LIGHT -> false
    ThemeMode.DARK -> true
}
