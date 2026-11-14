package com.cmptemplate.feature.onboarding.ui

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cmptemplate.core.datastore.UserPreferencesRepository
import com.cmptemplate.core.designsystem.i18n.AppLocale
import com.cmptemplate.core.designsystem.i18n.LocaleManager
import com.cmptemplate.core.model.ThemeMode
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * The steps, in order. WELCOME (language + theme) and DONE stay; replace APP with the product's
 * own first-run steps (a permission, an account, a first choice). Only what the app cannot work
 * without belongs here: everything else is in Settings/About.
 */
enum class OnboardingStep { WELCOME, APP, DONE }

data class OnboardingState(
    val step: OnboardingStep = OnboardingStep.WELCOME,
    val locale: AppLocale = AppLocale.Default,
    val theme: ThemeMode = ThemeMode.SYSTEM,
    /** Set once "done" is stored; the screen then leaves (state, not a one-off event). */
    val finished: Boolean = false,
) {
    val index: Int get() = step.ordinal
    val count: Int get() = OnboardingStep.entries.size
    val isLast: Boolean get() = step == OnboardingStep.entries.last()
}

sealed interface OnboardingAction {
    data object Next : OnboardingAction
    data object Back : OnboardingAction
    /** Jump back to a finished step (tapping it in the progress bar). Never forward. */
    data class GoTo(val index: Int) : OnboardingAction
    data class SetLocale(val locale: AppLocale) : OnboardingAction
    data class SetTheme(val mode: ThemeMode) : OnboardingAction
    /** Leave now, keeping the defaults for the remaining steps. Shown on every step but the last. */
    data object Skip : OnboardingAction
    data object Finish : OnboardingAction
}

/**
 * The step lives in SavedStateHandle: a language change recreates the Activity on Android and the
 * user must land on the same step (LockdownControl), and process death must not restart setup.
 * "Done" is stored in DataStore before [OnboardingState.finished] turns true, so leaving can never
 * outrun the write.
 */
class OnboardingViewModel(
    private val preferences: UserPreferencesRepository,
    private val localeManager: LocaleManager,
    private val saved: SavedStateHandle,
) : ViewModel() {
    private val stepIndex = saved.getStateFlow(KEY_STEP, 0)
    private val finished = MutableStateFlow(false)

    val state: StateFlow<OnboardingState> = combine(stepIndex, localeManager.locale, preferences.settings, finished) { i, locale, settings, done ->
        OnboardingState(stepOf(i), locale, settings.themeMode, done)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), OnboardingState(stepOf(stepIndex.value), localeManager.locale.value))

    fun onAction(action: OnboardingAction) {
        val current = stepIndex.value
        when (action) {
            OnboardingAction.Next -> saved[KEY_STEP] = (current + 1).coerceAtMost(OnboardingStep.entries.lastIndex)
            OnboardingAction.Back -> saved[KEY_STEP] = (current - 1).coerceAtLeast(0)
            is OnboardingAction.GoTo -> if (action.index in 0 until current) saved[KEY_STEP] = action.index
            is OnboardingAction.SetLocale -> localeManager.setLocale(action.locale)
            is OnboardingAction.SetTheme -> viewModelScope.launch { preferences.setThemeMode(action.mode) }
            OnboardingAction.Skip, OnboardingAction.Finish -> viewModelScope.launch {
                preferences.setOnboardingDone(true)
                finished.value = true
            }
        }
    }

    private fun stepOf(index: Int) = OnboardingStep.entries[index.coerceIn(0, OnboardingStep.entries.lastIndex)]

    private companion object {
        const val KEY_STEP = "onboarding_step"
    }
}
