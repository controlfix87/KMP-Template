package com.kmptemplate.core.designsystem.i18n

import android.content.Context
import org.koin.core.module.Module
import org.koin.dsl.module

/**
 * Reads and writes the same SharedPreferences file and key that [AndroidAppLocale] reads from
 * `attachBaseContext` — that native path runs long before Koin exists, so the two must agree on
 * storage by construction, which is why the names live on [AndroidAppLocale] rather than here.
 *
 * `commit()` rather than `apply()`: the value has to survive a process kill immediately after the
 * write (an APK reinstall right after changing language), and it is one small string.
 */
internal class AndroidLocaleStore(context: Context) : LocaleStore {
    private val prefs =
        context.applicationContext.getSharedPreferences(AndroidAppLocale.PREFS_NAME, Context.MODE_PRIVATE)

    override fun read(): String? = prefs.getString(AndroidAppLocale.KEY_APP_LOCALE, null)

    override fun write(code: String) {
        prefs.edit().putString(AndroidAppLocale.KEY_APP_LOCALE, code).commit()
    }
}

/** Resolves the `Context` that `startKoin { androidContext(...) }` registers in the app module. */
actual fun localeModule(): Module = module {
    single<LocaleStore> { AndroidLocaleStore(get<Context>()) }
    single { LocaleManager(get()) }
}
