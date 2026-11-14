package com.cmptemplate.app

import android.app.Application
import android.content.Context
import android.content.res.Configuration
import com.cmptemplate.core.designsystem.i18n.AndroidAppLocale
import com.cmptemplate.core.model.AppInfo
import com.cmptemplate.feature.about.report.CrashRecorder
import org.koin.dsl.module
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
class CMPTemplateApplication : Application() {

    override fun attachBaseContext(base: Context) {
        super.attachBaseContext(AndroidAppLocale.wrap(base))
    }

    override fun onCreate() {
        super.onCreate()
        // First, so a crash anywhere below is recorded and offered in the report screen next launch.
        CrashRecorder.install(this)
        AndroidAppLocale.apply(this, AndroidAppLocale.savedCode(this))
        startKoin {
            androidContext(this@CMPTemplateApplication)
            modules(appModules + androidHostModule(this@CMPTemplateApplication))
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

/** What the shared code needs from this host: who the build is (reports file tickets under [AppInfo.id]). */
fun androidHostModule(context: Context) = module {
    single {
        AppInfo(
            id = context.packageName,
            name = context.getString(R.string.app_name),
            version = BuildConfig.VERSION_NAME,
            code = BuildConfig.VERSION_CODE.toString(),
            flavor = "android/" + BuildConfig.BUILD_TYPE,
            platform = "android",
        )
    }
}
