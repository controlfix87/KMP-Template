package com.cmptemplate.core.datastore

import org.koin.core.module.Module
import org.koin.core.qualifier.named
import org.koin.dsl.bind
import org.koin.dsl.module

internal val PreferencesPath = named("userPreferencesPath")

/** Provides [PreferencesPath]: the absolute settings file path in app-private storage. */
internal expect fun preferencesPathModule(): Module

/** DataStore + [UserPreferencesRepository]. Android needs `androidContext(...)` in Koin. */
fun dataStoreModule(): Module = module {
    includes(preferencesPathModule())
    single { createPreferencesDataStore(get(PreferencesPath)) }
    single { DataStoreUserPreferences(get()) } bind UserPreferencesRepository::class
}
