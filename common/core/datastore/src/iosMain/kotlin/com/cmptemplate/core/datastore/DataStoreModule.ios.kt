package com.cmptemplate.core.datastore

import kotlinx.cinterop.ExperimentalForeignApi
import org.koin.core.module.Module
import org.koin.dsl.module
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSUserDomainMask

/** The app's Documents directory. Not run on Linux; see docs/VALIDATION.md. */
@OptIn(ExperimentalForeignApi::class)
internal actual fun preferencesPathModule(): Module = module {
    single(PreferencesPath) {
        val dir = NSFileManager.defaultManager.URLForDirectory(NSDocumentDirectory, NSUserDomainMask, null, true, null)
        requireNotNull(dir?.path) { "No documents directory" } + "/$USER_PREFERENCES_FILE"
    }
}
