package com.kmptemplate.core.designsystem.i18n

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalConfiguration
import java.util.Locale

/**
 * Reading [LocalConfiguration] is not dead code: it makes this composable recompose on every
 * configuration change, so the app's language is re-asserted into `Locale.getDefault()` immediately
 * after the framework has replaced it with the device's. Without that read, a rotation on a device
 * whose language differs from the app's leaves the process on the device language.
 *
 * The [LaunchedEffect] is the one place a language change from the picker reaches the platform's
 * per-app locale service. It runs only when the tag actually changes, and
 * [AndroidAppLocale.syncSystemPerAppLocale] is a no-op when the platform already agrees, so this
 * cannot loop even though assigning the per-app locale is itself a configuration change.
 */
@Composable
internal actual fun applyLocale(languageTag: String) {
    @Suppress("UNUSED_VARIABLE")
    val configuration = LocalConfiguration.current
    val context: Context = LocalContext.current
    forceLocale(languageTag)
    LaunchedEffect(languageTag) {
        AndroidAppLocale.syncSystemPerAppLocale(context.applicationContext, languageTag)
    }
}

internal actual fun forceLocale(languageTag: String) {
    AndroidAppLocale.setProcessDefault(AndroidAppLocale.localeOf(languageTag))
}

internal actual fun currentPlatformLanguage(): String =
    normalizeLanguageSubtag(Locale.getDefault().language)
