package com.kmptemplate.core.designsystem.i18n

import org.koin.core.module.Module
import org.koin.dsl.module
import platform.Foundation.NSUserDefaults

/**
 * Kept under the app's own key rather than reusing `AppleLanguages`: that key is the OS's, other
 * code and the system can write it, and this value must only ever change through the picker.
 *
 * Not run on this host — see docs/VALIDATION.md.
 */
internal class NSUserDefaultsLocaleStore : LocaleStore {
    private val defaults = NSUserDefaults.standardUserDefaults

    override fun read(): String? = defaults.stringForKey(KEY_APP_LOCALE)

    override fun write(code: String) {
        defaults.setObject(code, forKey = KEY_APP_LOCALE)
        defaults.synchronize()
    }

    private companion object {
        const val KEY_APP_LOCALE = "app_locale"
    }
}

actual fun localeModule(): Module = module {
    single<LocaleStore> { NSUserDefaultsLocaleStore() }
    single { LocaleManager(get()) }
}
