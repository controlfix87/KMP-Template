package com.kmptemplate.app.di

import com.kmptemplate.core.network.createHttpClient
import org.koin.dsl.module
import org.koin.dsl.onClose

/**
 * Everything below is a `single` shared across the whole app. Feature modules
 * bring their own `di` module (see featureExampleModule) that reach into
 * these via Koin's `get()` -- never construct an `HttpClient` a second time
 * inside a feature.
 */
fun coreNetworkModule(baseUrl: String, enableLogging: Boolean) = module {
    single { createHttpClient(baseUrl = baseUrl, enableLogging = enableLogging) } onClose { it?.close() }
}
