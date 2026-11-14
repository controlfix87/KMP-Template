package com.cmptemplate.feature.about.report

import org.koin.core.module.Module
import org.koin.dsl.module
import platform.Foundation.NSLocale
import platform.Foundation.currentLocale
import platform.Foundation.localeIdentifier
import platform.UIKit.UIDevice
import platform.UIKit.UIUserInterfaceIdiomPad

/**
 * Device details only. Crash capture on iOS needs a signal handler (or a crash SDK); the app's
 * own log is not readable. Not run on this host (Linux); see docs/VALIDATION.md.
 */
internal class IosDiagnostics : Diagnostics {
    override fun device(): DeviceInfo {
        val d = UIDevice.currentDevice
        return DeviceInfo(
            manufacturer = "Apple",
            model = d.model,
            device = d.name,
            os = "${d.systemName} ${d.systemVersion}",
            type = if (d.userInterfaceIdiom == UIUserInterfaceIdiomPad) "tablet" else "phone",
            language = NSLocale.currentLocale.localeIdentifier,
        )
    }

    override suspend fun recentLog(): String? = null
    override fun lastCrash(): CrashRecord? = null
    override fun clearCrash() = Unit
}

actual fun diagnosticsModule(): Module = module {
    single<Diagnostics> { IosDiagnostics() }
}
