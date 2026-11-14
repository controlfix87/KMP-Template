package com.cmptemplate.core.model

/** Light/dark choice; [SYSTEM] follows the device until the user picks one. */
enum class ThemeMode { SYSTEM, LIGHT, DARK }

/** The user's app-wide settings, as stored by `:common:core:datastore`. */
data class UserSettings(
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val onboardingDone: Boolean = false,
)
