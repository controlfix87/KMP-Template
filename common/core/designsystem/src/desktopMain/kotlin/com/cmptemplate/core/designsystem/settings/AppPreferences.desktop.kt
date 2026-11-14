package com.cmptemplate.core.designsystem.settings

import com.cmptemplate.core.designsystem.ThemeManager
import org.koin.core.module.Module
import org.koin.dsl.module
import java.util.prefs.Preferences

/** The user's own preference node (registry on Windows, plist on macOS, ~/.java on Linux). */
internal fun desktopPreferenceNode(): Preferences = Preferences.userRoot().node("cmptemplate")

internal class JavaPreferencesStore(private val node: Preferences = desktopPreferenceNode().node("settings")) : AppPreferences {
    override fun getString(key: String): String? = node.get(key, null)
    override fun putString(key: String, value: String) { node.put(key, value); node.flush() }
    override fun remove(key: String) { node.remove(key); node.flush() }
}

actual fun preferencesModule(): Module = module {
    single<AppPreferences> { JavaPreferencesStore() }
    single { ThemeManager(get()) }
}
