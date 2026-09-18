package com.kmptemplate.core.designsystem.i18n

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Where the chosen language is persisted. Deliberately **synchronous**: on Android the value has to
 * be readable from `Application.attachBaseContext`, which runs before coroutines, DI or anything
 * else exists, and a value that arrives one frame late there means one frame in the wrong language.
 */
interface LocaleStore {
    fun read(): String?

    fun write(code: String)
}

/**
 * Owns the user's language choice and nothing else.
 *
 * The only way the value changes is [setLocale], called from the language picker. There is no
 * "follow system" mode, no toggle shortcut and no path that reads the device language — that is the
 * whole point (see [DEFAULT_LOCALE_CODE]). Applying the choice to the platform is not done here;
 * see [AppLocaleProvider] and, on Android, `AndroidAppLocale`.
 */
class LocaleManager(private val store: LocaleStore) {
    private val _locale = MutableStateFlow(loadSaved())
    val locale: StateFlow<AppLocale> = _locale.asStateFlow()

    fun setLocale(locale: AppLocale) {
        store.write(locale.code)
        _locale.value = locale
    }

    /**
     * Resolves the stored code, repairing a missing **or unrecognised** one to
     * [DEFAULT_LOCALE_CODE] and writing it straight back.
     *
     * Writing back matters as much as returning the right value: on Android the native layer reads
     * this same key directly during `attachBaseContext`, long before this class is constructed. If
     * "no preference" stayed unwritten, the native layer would have nothing to assert and would sit
     * on the device language until the first configuration change — which is exactly the drift this
     * whole package exists to prevent.
     */
    private fun loadSaved(): AppLocale {
        AppLocale.ofCode(store.read())?.let { return it }
        store.write(DEFAULT_LOCALE_CODE)
        return AppLocale.Default
    }
}
