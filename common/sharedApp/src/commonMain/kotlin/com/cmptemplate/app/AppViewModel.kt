package com.cmptemplate.app

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cmptemplate.core.datastore.UserPreferencesRepository
import com.cmptemplate.core.model.ThemeMode
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

sealed interface AppUiState {
    /** Settings not read yet (one DataStore read at start): draw nothing rather than the wrong theme. */
    data object Loading : AppUiState
    data class Ready(val theme: ThemeMode, val onboardingDone: Boolean) : AppUiState
}

/** App-wide state the root composable needs before it can draw anything. */
class AppViewModel(preferences: UserPreferencesRepository) : ViewModel() {
    val state: StateFlow<AppUiState> = preferences.settings
        .map { AppUiState.Ready(it.themeMode, it.onboardingDone) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), AppUiState.Loading)
}
