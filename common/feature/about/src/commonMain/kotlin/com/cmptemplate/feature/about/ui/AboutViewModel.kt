package com.cmptemplate.feature.about.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cmptemplate.core.datastore.UserPreferencesRepository
import com.cmptemplate.core.designsystem.i18n.AppLocale
import com.cmptemplate.core.designsystem.i18n.LocaleManager
import com.cmptemplate.core.model.AppInfo
import com.cmptemplate.core.model.ThemeMode
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class AboutUiState(val app: AppInfo, val locale: AppLocale, val theme: ThemeMode = ThemeMode.SYSTEM)

sealed interface AboutAction {
    data class SetLocale(val locale: AppLocale) : AboutAction
    data class SetTheme(val mode: ThemeMode) : AboutAction
    /** Clears "onboarding done"; the caller then navigates to onboarding. */
    data object RunSetupAgain : AboutAction
}

class AboutViewModel(
    app: AppInfo,
    private val localeManager: LocaleManager,
    private val preferences: UserPreferencesRepository,
) : ViewModel() {
    val state: StateFlow<AboutUiState> = combine(localeManager.locale, preferences.settings) { locale, settings ->
        AboutUiState(app, locale, settings.themeMode)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), AboutUiState(app, localeManager.locale.value))

    fun onAction(action: AboutAction) {
        when (action) {
            is AboutAction.SetLocale -> localeManager.setLocale(action.locale)
            is AboutAction.SetTheme -> viewModelScope.launch { preferences.setThemeMode(action.mode) }
            AboutAction.RunSetupAgain -> viewModelScope.launch { preferences.setOnboardingDone(false) }
        }
    }
}
