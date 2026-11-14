package com.cmptemplate.core.designsystem.settings

import org.koin.core.module.Module

/**
 * Small synchronous key-value settings (theme, onboarding done, flags). Not for data: use a
 * database (docs/PERSISTENCE.md). The language is NOT stored here -- Android reads it before Koin
 * exists, see i18n/LocaleManager.
 */
interface AppPreferences {
    fun getString(key: String): String?
    fun putString(key: String, value: String)
    fun getBoolean(key: String, default: Boolean = false): Boolean = getString(key)?.toBooleanStrictOrNull() ?: default
    fun putBoolean(key: String, value: Boolean) = putString(key, value.toString())
    fun remove(key: String)
}

/** In-memory preferences for tests and previews. */
class InMemoryPreferences(initial: Map<String, String> = emptyMap()) : AppPreferences {
    private val values = initial.toMutableMap()
    override fun getString(key: String): String? = values[key]
    override fun putString(key: String, value: String) { values[key] = value }
    override fun remove(key: String) { values.remove(key) }
}

/** SharedPreferences / NSUserDefaults / java.util.prefs, plus [com.cmptemplate.core.designsystem.ThemeManager]. */
expect fun preferencesModule(): Module
