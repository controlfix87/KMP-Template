package com.cmptemplate.feature.onboarding.ui

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.cmptemplate.core.designsystem.settings.AppPreferences
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import androidx.lifecycle.viewModelScope

/**
 * The steps, in order. WELCOME (language + theme) and DONE stay; replace APP with the product's
 * own first-run steps (a permission, an account, a first choice). Only what the app cannot work
 * without belongs here: everything else is in Settings/About.
 */
enum class OnboardingStep { WELCOME, APP, DONE }

data class OnboardingState(val step: OnboardingStep = OnboardingStep.WELCOME) {
    val index: Int get() = step.ordinal
    val count: Int get() = OnboardingStep.entries.size
    val isLast: Boolean get() = step == OnboardingStep.entries.last()
}

sealed interface OnboardingAction {
    data object Next : OnboardingAction
    data object Back : OnboardingAction
    /** Jump back to a finished step (tapping it in the progress bar). Never forward. */
    data class GoTo(val index: Int) : OnboardingAction
    /** Leave now, keeping the defaults for the remaining steps. Shown on every step but the last. */
    data object Skip : OnboardingAction
    data object Finish : OnboardingAction
}

/**
 * The step lives in SavedStateHandle: a language change recreates the Activity on Android and the
 * user must land on the same step (LockdownControl), and process death must not restart setup.
 */
class OnboardingViewModel(
    private val preferences: AppPreferences,
    private val saved: SavedStateHandle,
) : ViewModel() {
    private val stepIndex = saved.getStateFlow(KEY_STEP, 0)

    val state: StateFlow<OnboardingState> = stepIndex
        .map { OnboardingState(OnboardingStep.entries[it.coerceIn(0, OnboardingStep.entries.lastIndex)]) }
        .stateIn(viewModelScope, SharingStarted.Eagerly, OnboardingState(OnboardingStep.entries[stepIndex.value.coerceIn(0, OnboardingStep.entries.lastIndex)]))

    /** Returns true when onboarding is finished and the caller should leave it. */
    fun onAction(action: OnboardingAction): Boolean {
        val current = stepIndex.value
        when (action) {
            OnboardingAction.Next -> saved[KEY_STEP] = (current + 1).coerceAtMost(OnboardingStep.entries.lastIndex)
            OnboardingAction.Back -> saved[KEY_STEP] = (current - 1).coerceAtLeast(0)
            is OnboardingAction.GoTo -> if (action.index in 0 until current) saved[KEY_STEP] = action.index
            OnboardingAction.Skip, OnboardingAction.Finish -> {
                markDone(preferences)
                saved[KEY_STEP] = 0
                return true
            }
        }
        return false
    }

    companion object {
        private const val KEY_STEP = "onboarding_step"
        const val KEY_DONE = "onboarding_done"

        fun isDone(preferences: AppPreferences): Boolean = preferences.getBoolean(KEY_DONE)
        fun markDone(preferences: AppPreferences) = preferences.putBoolean(KEY_DONE, true)
        /** "Run setup again" from About. */
        fun reset(preferences: AppPreferences) = preferences.remove(KEY_DONE)
    }
}
