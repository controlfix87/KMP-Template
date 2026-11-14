package com.cmptemplate.core.datastore

import android.content.Context
import org.koin.core.module.Module
import org.koin.dsl.module

internal actual fun preferencesPathModule(): Module = module {
    single(PreferencesPath) { get<Context>().filesDir.resolve("datastore/$USER_PREFERENCES_FILE").absolutePath }
}
