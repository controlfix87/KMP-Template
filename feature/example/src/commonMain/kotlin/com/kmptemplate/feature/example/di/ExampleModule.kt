package com.kmptemplate.feature.example.di

import com.kmptemplate.feature.example.data.ExampleRepositoryImpl
import com.kmptemplate.feature.example.domain.ExampleRepository
import com.kmptemplate.feature.example.ui.ExampleViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

/**
 * `HttpClient` comes from `coreNetworkModule` (see :androidApp's DI wiring),
 * which must already be loaded when this module is, same as every other
 * feature module.
 */
val featureExampleModule = module {
    single<ExampleRepository> { ExampleRepositoryImpl(get()) }
    viewModel { ExampleViewModel(get()) }
}
