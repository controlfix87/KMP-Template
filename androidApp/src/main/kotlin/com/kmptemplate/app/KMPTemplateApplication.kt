package com.kmptemplate.app

import android.app.Application
import android.content.Context
import android.content.res.Configuration
import com.kmptemplate.core.designsystem.i18n.AndroidAppLocale
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

/**
 * The locale overrides here are not boilerplate -- they are the fix for a specific, well-hidden bug.
 *
 * Android re-derives the process-global `Locale.getDefault()` (which Compose Multiplatform resolves
 * every `stringResource()` from) out of the **Application** context's configuration, on process bind
 * and on every configuration update the process receives. An app that forces its language only on
 * the Activity leaves this context on the *device* language, so rotations, dark-mode toggles and
 * background configuration updates silently reset the app to it -- and because Compose re-reads the
 * locale on every recomposition, the wrong language surfaces later, on whatever screen happens to
 * compose next. See `AndroidAppLocale` for the full layering.
 */
class KMPTemplateApplication : Application() {

    override fun attachBaseContext(base: Context) {
        super.attachBaseContext(AndroidAppLocale.wrap(base))
    }

    override fun onCreate() {
        super.onCreate()
        AndroidAppLocale.apply(this, AndroidAppLocale.savedCode(this))
        startKoin {
            androidContext(this@KMPTemplateApplication)
            modules(appModules)
        }
    }

    /**
     * Process-level configuration changes reach the Application even when no Activity is resumed.
     * Patch before `super` so anything else listening already sees the corrected locale.
     */
    override fun onConfigurationChanged(newConfig: Configuration) {
        AndroidAppLocale.patch(newConfig, this)
        super.onConfigurationChanged(newConfig)
    }
}
