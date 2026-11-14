package com.cmptemplate.app

import androidx.compose.ui.window.ComposeUIViewController
import com.cmptemplate.core.model.AppInfo
import org.koin.compose.KoinApplication
import org.koin.dsl.module
import platform.Foundation.NSBundle

/** Who this build is, from Info.plist (reports file tickets under the bundle id). Not run on Linux. */
private fun iosHostModule() = module {
    single {
        val info = NSBundle.mainBundle
        fun key(name: String) = info.objectForInfoDictionaryKey(name) as? String
        AppInfo(
            id = info.bundleIdentifier ?: "com.cmptemplate.app",
            name = key("CFBundleDisplayName") ?: key("CFBundleName") ?: "CMPTemplate",
            version = key("CFBundleShortVersionString") ?: "0",
            code = key("CFBundleVersion") ?: "0",
            flavor = "ios",
            platform = "ios",
        )
    }
}

fun MainViewController() = ComposeUIViewController {
    KoinApplication(application = { modules(appModules + iosHostModule()) }) { App() }
}
