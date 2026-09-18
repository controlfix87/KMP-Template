package com.kmptemplate.core.designsystem.i18n

import androidx.compose.runtime.Composable
import platform.Foundation.NSUserDefaults

/**
 * iOS has no equivalent of the Android bug this package exists for: there is no `ActivityThread`
 * re-deriving a process-global locale behind the app's back, so nothing here has to fight the OS.
 * What it does instead is record the choice where iOS itself looks for it.
 *
 * **Known limitation, and it is a real one.** Compose Multiplatform 1.11.1 resolves
 * `stringResource()` on iOS from `NSLocale.currentLocale`, and the only hook that could redirect it
 * (`LocalComposeEnvironment`) is `internal` in that version — verified by compiling against it.
 * `ResourceEnvironment`'s constructor is internal too, so there is no supported way to hand the
 * resolver a different locale. Writing `AppleLanguages` below is what Foundation reads; whether it
 * invalidates `NSLocale.currentLocale` within the same process is a Foundation implementation
 * detail, so treat "strings change on the next launch" as the guaranteed behaviour and anything
 * sooner as a bonus.
 *
 * Layout direction is unaffected by all of this: it comes from [AppLocale.direction] through
 * `LocalLayoutDirection` in [AppLocaleProvider], so it flips immediately on every platform.
 *
 * None of this file has been run. This host is Linux; see docs/VALIDATION.md.
 */
private const val APPLE_LANGUAGES_KEY = "AppleLanguages"

@Composable
internal actual fun applyLocale(languageTag: String) {
    forceLocale(languageTag)
}

internal actual fun forceLocale(languageTag: String) {
    if (currentPlatformLanguage() == languageTag) return
    val defaults = NSUserDefaults.standardUserDefaults
    defaults.setObject(listOf(languageTag), forKey = APPLE_LANGUAGES_KEY)
    defaults.synchronize()
}

/**
 * Reports what this app last wrote to `AppleLanguages`, **not** `NSLocale.currentLocale`.
 *
 * Deliberate: `currentLocale` reflects the *device's* language preference, which the app cannot set
 * and must not follow. Comparing against it would make [AppLocaleProvider]'s drift check report a
 * mismatch on every resume for any user whose device language differs from their app language, and
 * rebuild the whole tree each time to no effect.
 */
internal actual fun currentPlatformLanguage(): String {
    val stored = NSUserDefaults.standardUserDefaults
        .stringArrayForKey(APPLE_LANGUAGES_KEY)
        ?.firstOrNull() as? String
        ?: return ""
    return normalizeLanguageSubtag(stored.substringBefore('-'))
}
