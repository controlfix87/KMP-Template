package com.cmptemplate.core.designsystem.i18n

import androidx.compose.runtime.Composable
import java.util.Locale

/**
 * Compose Multiplatform on the desktop resolves `stringResource()` from `Locale.getDefault()`, and
 * nothing on the JVM resets it behind the app's back, so setting it is the whole job.
 */
@Composable
internal actual fun applyLocale(languageTag: String) {
    forceLocale(languageTag)
}

internal actual fun forceLocale(languageTag: String) {
    if (currentPlatformLanguage() != languageTag) Locale.setDefault(Locale.forLanguageTag(languageTag))
}

internal actual fun currentPlatformLanguage(): String = normalizeLanguageSubtag(Locale.getDefault().language)
