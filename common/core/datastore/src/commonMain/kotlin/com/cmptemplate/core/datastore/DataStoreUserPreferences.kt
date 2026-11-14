package com.cmptemplate.core.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.core.handlers.ReplaceFileCorruptionHandler
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import com.cmptemplate.core.model.ThemeMode
import com.cmptemplate.core.model.UserSettings
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import okio.Path.Companion.toPath

/** File name of the settings store, inside each platform's app-private directory. */
const val USER_PREFERENCES_FILE = "user_settings.preferences_pb"

/**
 * Creates the one DataStore for [path] (absolute). A corrupt file is replaced with defaults rather
 * than crashing the app on every start.
 */
fun createPreferencesDataStore(path: String): DataStore<Preferences> = PreferenceDataStoreFactory.createWithPath(
    corruptionHandler = ReplaceFileCorruptionHandler { emptyPreferences() },
    produceFile = { path.toPath() },
)

class DataStoreUserPreferences(private val store: DataStore<Preferences>) : UserPreferencesRepository {
    override val settings: Flow<UserSettings> = store.data
        // An unreadable file must not take the UI down: fall back to defaults (and keep going).
        .catch { emit(emptyPreferences()) }
        .map { p ->
            UserSettings(
                themeMode = ThemeMode.entries.firstOrNull { it.name == p[THEME] } ?: ThemeMode.SYSTEM,
                onboardingDone = p[ONBOARDING_DONE] ?: false,
            )
        }
        .distinctUntilChanged()

    override suspend fun setThemeMode(mode: ThemeMode) {
        store.edit { it[THEME] = mode.name }
    }

    override suspend fun setOnboardingDone(done: Boolean) {
        store.edit { it[ONBOARDING_DONE] = done }
    }

    private companion object {
        // Stored names are the contract with existing installs: rename = migration.
        val THEME = stringPreferencesKey("theme_mode")
        val ONBOARDING_DONE = booleanPreferencesKey("onboarding_done")
    }
}
