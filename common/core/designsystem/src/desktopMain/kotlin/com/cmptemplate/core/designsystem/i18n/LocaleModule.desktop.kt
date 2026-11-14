package com.cmptemplate.core.designsystem.i18n

import java.util.prefs.Preferences
import org.koin.core.module.Module
import org.koin.dsl.module

internal class JavaPreferencesLocaleStore : LocaleStore {
    /** The user's preference node (registry on Windows, plist on macOS, ~/.java on Linux). */
    private val node = Preferences.userRoot().node("cmptemplate")

    override fun read(): String? = node.get(KEY_APP_LOCALE, null)

    override fun write(code: String) {
        node.put(KEY_APP_LOCALE, code)
        node.flush()
    }

    private companion object {
        const val KEY_APP_LOCALE = "app_locale"
    }
}

actual fun localeModule(): Module = module {
    single<LocaleStore> { JavaPreferencesLocaleStore() }
    single { LocaleManager(get()) }
}
