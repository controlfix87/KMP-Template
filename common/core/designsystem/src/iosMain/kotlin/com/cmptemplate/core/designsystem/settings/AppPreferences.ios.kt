package com.cmptemplate.core.designsystem.settings

import com.cmptemplate.core.designsystem.ThemeManager
import org.koin.core.module.Module
import org.koin.dsl.module
import platform.Foundation.NSUserDefaults

/** Not run on this host (Linux); see docs/VALIDATION.md. */
internal class UserDefaultsStore : AppPreferences {
    private val defaults = NSUserDefaults.standardUserDefaults
    override fun getString(key: String): String? = defaults.stringForKey(PREFIX + key)
    override fun putString(key: String, value: String) { defaults.setObject(value, forKey = PREFIX + key) }
    override fun remove(key: String) { defaults.removeObjectForKey(PREFIX + key) }

    private companion object {
        const val PREFIX = "settings."
    }
}

actual fun preferencesModule(): Module = module {
    single<AppPreferences> { UserDefaultsStore() }
    single { ThemeManager(get()) }
}
