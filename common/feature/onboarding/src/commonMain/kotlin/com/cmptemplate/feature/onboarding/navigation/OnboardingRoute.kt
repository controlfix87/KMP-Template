package com.cmptemplate.feature.onboarding.navigation

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.cmptemplate.feature.onboarding.ui.OnboardingRoot
import kotlinx.serialization.Serializable

@Serializable
data object OnboardingRoute : NavKey

fun EntryProviderScope<NavKey>.onboardingEntries(onDone: () -> Unit) {
    entry<OnboardingRoute> { OnboardingRoot(onDone) }
}
