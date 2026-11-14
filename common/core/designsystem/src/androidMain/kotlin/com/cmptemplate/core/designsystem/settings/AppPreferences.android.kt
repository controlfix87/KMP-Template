package com.cmptemplate.core.designsystem.settings

import android.content.Context
import com.cmptemplate.core.designsystem.ThemeManager
import org.koin.core.module.Module
import org.koin.dsl.module

/** `commit()`: a setting must survive a process kill right after it is changed. */
internal class SharedPreferencesStore(context: Context) : AppPreferences {
    private val prefs = context.applicationContext.getSharedPreferences("app_settings", Context.MODE_PRIVATE)
    override fun getString(key: String): String? = prefs.getString(key, null)
    override fun putString(key: String, value: String) { prefs.edit().putString(key, value).commit() }
    override fun remove(key: String) { prefs.edit().remove(key).commit() }
}

actual fun preferencesModule(): Module = module {
    single<AppPreferences> { SharedPreferencesStore(get<Context>()) }
    single { ThemeManager(get()) }
}
