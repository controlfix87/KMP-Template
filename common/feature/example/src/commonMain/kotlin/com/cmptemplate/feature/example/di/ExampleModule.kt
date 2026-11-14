package com.cmptemplate.feature.example.di

import com.cmptemplate.feature.example.data.DemoExampleRepository
import com.cmptemplate.feature.example.domain.ExampleRepository
import com.cmptemplate.feature.example.ui.ExampleViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val featureExampleModule = module {
    single<ExampleRepository> { DemoExampleRepository() }
    viewModel { ExampleViewModel(get(), get()) }
}
