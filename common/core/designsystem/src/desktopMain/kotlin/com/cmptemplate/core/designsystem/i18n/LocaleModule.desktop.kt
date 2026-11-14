package com.cmptemplate.core.designsystem.i18n

import com.cmptemplate.core.designsystem.settings.desktopPreferenceNode
import org.koin.core.module.Module
import org.koin.dsl.module

internal class JavaPreferencesLocaleStore : LocaleStore {
    private val node = desktopPreferenceNode()

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
