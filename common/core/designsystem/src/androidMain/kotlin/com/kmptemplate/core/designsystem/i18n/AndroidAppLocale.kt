package com.kmptemplate.core.designsystem.i18n

import android.content.Context
import android.content.res.Configuration
import android.os.Build
import android.os.LocaleList
import android.util.Log
import java.util.Locale

/**
 * The single Android-side owner of "what language is this app in".
 *
 * ## The bug this exists to prevent
 *
 * `java.util.Locale.getDefault()` is the *framework's* variable, not the app's. Compose
 * Multiplatform resolves every `stringResource()` from it, re-reading it on every recomposition, so
 * whoever owns `Locale.getDefault()` owns the app's language.
 *
 * `ActivityThread.updateLocaleListFromAppContext()` re-derives it from the **Application** context's
 * configuration, and runs on process bind and on every configuration update the process receives:
 * rotation, dark-mode toggle, density/font-scale change, window resize, a system locale change, and
 * updates delivered while the app is backgrounded. Forcing the locale only on the *Activity* — the
 * usual advice, and what most samples do — leaves the Application context on the **device**
 * language, so every one of those events quietly resets the process back to it. The UI does not
 * flip at that instant; it flips at the next recomposition of any text, which is what makes the bug
 * look random instead of reproducible.
 *
 * ## The fix, in layers, outermost first
 *
 * 1. **[syncSystemPerAppLocale] (API 33+)** — hand the choice to the platform's per-app language
 *    service. From then on the *system* stamps the language into every Configuration it builds for
 *    this app, including the Application's, so the re-derivation above yields the app's language.
 *    This is the real fix; everything below it is what covers API 26-32.
 * 2. **[wrap] on the Application's base context** — `createConfigurationContext` registers a
 *    persistent override in `ResourcesManager`, so the Application's `Resources` keep the language
 *    across configuration updates and the re-derivation reads it back.
 * 3. **[wrap] on the Activity's base context** — so `resources.getString`, date/number formatting
 *    and anything else reading `context.resources` agree with Compose from the first frame.
 * 4. **[patch] in `onConfigurationChanged`, [reassert] in `onResume`** — event-driven repair.
 * 5. **The drift check in [AppLocaleProvider]** — last line of defence, and the only layer that
 *    makes an already-composed tree reload its strings.
 *
 * The language is never *decided* here: [savedCode] only reads what the picker wrote.
 */
object AndroidAppLocale {
    private const val TAG = "AppLocale"

    /** Shared with [AndroidLocaleStore] so the two can never read different keys. */
    internal const val PREFS_NAME = "app_locale_prefs"
    internal const val KEY_APP_LOCALE = "app_locale"

    /**
     * The persisted language code, repaired to [DEFAULT_LOCALE_CODE] (and written back) when absent
     * or not a language this build ships.
     *
     * Committed synchronously, not `apply()`: this is read from `attachBaseContext`, and an
     * asynchronous write there can be lost if the process is killed moments later by an APK
     * reinstall — leaving the next launch to fall back to the device language.
     */
    fun savedCode(context: Context): String {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        AppLocale.ofCode(prefs.getString(KEY_APP_LOCALE, null))?.let { return it.code }
        prefs.edit().putString(KEY_APP_LOCALE, DEFAULT_LOCALE_CODE).commit()
        return DEFAULT_LOCALE_CODE
    }

    fun localeOf(code: String): Locale = Locale.forLanguageTag(code)

    /**
     * Pin the process-wide default to [locale].
     *
     * `Locale.setDefault` alone is enough: `LocaleList.getDefault()` — what Compose reads on
     * API 24+ — rebuilds itself with the new default at the head whenever `Locale.getDefault()`
     * changes, and `LocaleList.setDefault` is not public API.
     */
    fun setProcessDefault(locale: Locale) {
        if (Locale.getDefault() != locale) Locale.setDefault(locale)
    }

    /**
     * Wrap a base context so its `Resources`/`Configuration` carry the app language, and pin the
     * process default to match. Call from `attachBaseContext` of both the Application and the
     * Activity.
     */
    fun wrap(base: Context): Context {
        val locale = localeOf(savedCode(base))
        setProcessDefault(locale)
        val config = Configuration(base.resources.configuration).apply { setLocale(locale) }
        return base.createConfigurationContext(config)
    }

    /**
     * Patch an incoming configuration in `onConfigurationChanged` **before** calling `super`, so the
     * framework propagates the app's locale — not the device's — down to Compose's
     * `LocalConfiguration`.
     */
    fun patch(newConfig: Configuration, context: Context) {
        val locale = localeOf(savedCode(context))
        setProcessDefault(locale)
        newConfig.setLocale(locale)
    }

    /** Repair the process default if something reset it. Cheap: a comparison, and a write only on drift. */
    fun reassert(context: Context) {
        val expected = localeOf(savedCode(context))
        if (Locale.getDefault() != expected) {
            Log.w(TAG, "process locale drifted to ${Locale.getDefault().toLanguageTag()}; restoring ${expected.toLanguageTag()}")
            Locale.setDefault(expected)
        }
    }

    /** Apply [code] everywhere the platform can hold it. */
    fun apply(context: Context, code: String) {
        setProcessDefault(localeOf(code))
        syncSystemPerAppLocale(context, code)
    }

    /**
     * API 33+: register the choice with `android.app.LocaleManager` so the platform itself applies
     * it to every Configuration it hands this app, on every process start and every config change.
     *
     * Guarded by an equality check because assigning `applicationLocales` is itself a configuration
     * change; an unconditional write on every startup would cost an extra config change (and, on an
     * Activity without `locale` in `configChanges`, an extra recreation) every launch.
     *
     * No `android:localeConfig` is declared in the manifest, on purpose: it would surface a *system*
     * language picker for this app whose choice this code overwrites on the next launch. If you do
     * want the system picker to be authoritative, declare it and read `applicationLocales` back into
     * [LocaleManager] at startup instead of pushing to it — but pick one owner, not both.
     */
    fun syncSystemPerAppLocale(context: Context, code: String) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return
        val service = context.getSystemService(android.app.LocaleManager::class.java) ?: return
        val desired = LocaleList.forLanguageTags(code)
        if (service.applicationLocales.toLanguageTags() != desired.toLanguageTags()) {
            Log.i(TAG, "setting per-app locale to $code")
            service.applicationLocales = desired
        }
    }
}
