package com.cmptemplate.feature.onboarding.di

import com.cmptemplate.feature.onboarding.ui.OnboardingViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val featureOnboardingModule = module {
    viewModel { OnboardingViewModel(get(), get()) }
}
