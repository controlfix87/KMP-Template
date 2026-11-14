package com.cmptemplate.core.datastore

import com.cmptemplate.core.model.ThemeMode
import com.cmptemplate.core.model.UserSettings
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update

/**
 * App-wide user settings. Reads are a [Flow] (DataStore is asynchronous: never block for a value);
 * writes are suspending and atomic. ViewModels expose them to the UI as `StateFlow`.
 *
 * The app language is deliberately NOT here: Android must read it synchronously in
 * `attachBaseContext`, before DI or coroutines exist (see designsystem `i18n/AndroidAppLocale`).
 */
interface UserPreferencesRepository {
    val settings: Flow<UserSettings>
    suspend fun setThemeMode(mode: ThemeMode)
    suspend fun setOnboardingDone(done: Boolean)
}

/** For tests and previews: same contract, kept in memory. */
class InMemoryUserPreferences(initial: UserSettings = UserSettings()) : UserPreferencesRepository {
    override val settings = MutableStateFlow(initial)
    override suspend fun setThemeMode(mode: ThemeMode) = settings.update { it.copy(themeMode = mode) }
    override suspend fun setOnboardingDone(done: Boolean) = settings.update { it.copy(onboardingDone = done) }
}
