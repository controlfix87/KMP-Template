package com.cmptemplate.core.datastore

import org.koin.core.module.Module
import org.koin.dsl.module
import java.io.File

/** `~/.cmptemplate/` (the generator renames it per project). */
internal actual fun preferencesPathModule(): Module = module {
    single(PreferencesPath) {
        File(System.getProperty("user.home"), ".cmptemplate/$USER_PREFERENCES_FILE").also { it.parentFile?.mkdirs() }.absolutePath
    }
}
