package com.kmptemplate.feature.example.di

import com.kmptemplate.feature.example.data.DemoExampleRepository
import com.kmptemplate.feature.example.domain.ExampleRepository
import com.kmptemplate.feature.example.ui.ExampleViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val featureExampleModule = module {
    single<ExampleRepository> { DemoExampleRepository() }
    viewModel { ExampleViewModel(get(), get()) }
}
