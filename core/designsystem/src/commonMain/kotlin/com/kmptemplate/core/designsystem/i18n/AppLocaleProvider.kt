package com.kmptemplate.core.designsystem.i18n

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.repeatOnLifecycle

/**
 * The single place the chosen language becomes visible UI. Wrap the app root in it, inside the
 * theme, and everything below resolves `stringResource()` and `LocalLayoutDirection` from the same
 * [locale] — they cannot drift apart, because there is only one value.
 *
 * ## Why there is a drift check and not just an "apply once"
 *
 * Compose Multiplatform resolves every `stringResource()` from the **process-global** platform
 * locale (on Android, `java.util.Locale.getDefault()` via
 * `androidx.compose.ui.text.intl.Locale.current`), re-reading it on *every* recomposition. And on
 * Android that variable belongs to the framework, not to the app: `ActivityThread` re-derives it
 * from the Application context's configuration on process bind and on every configuration update
 * the process receives — rotation, dark mode, font scale, window resize, and updates delivered
 * while the app is backgrounded.
 *
 * So an app that asserts its language only when the composable recomposes will, sooner or later,
 * find the process back on the device language with no recomposition to undo it. Nothing repaints
 * at that moment; the wrong language leaks into the *next* thing composed — a new screen, a dialog,
 * a list item scrolling in. That is what "the app randomly switched language" actually is.
 *
 * Prevention belongs in the platform layer (on Android: `AndroidAppLocale`, which overrides the
 * Application context and, on API 33+, registers the choice with the platform's per-app locale
 * service). This check is the last line of defence: on each RESUMED transition, if the platform no
 * longer reports the chosen language, re-assert it and bump the key so the whole tree reloads its
 * strings at once instead of drifting screen by screen. It costs one string comparison per resume.
 */
@Composable
fun AppLocaleProvider(locale: AppLocale, content: @Composable () -> Unit) {
    applyLocale(locale.code)

    var healGeneration by remember { mutableIntStateOf(0) }
    val lifecycleOwner = LocalLifecycleOwner.current
    LaunchedEffect(locale, lifecycleOwner) {
        lifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.RESUMED) {
            if (currentPlatformLanguage() != locale.code) {
                forceLocale(locale.code)
                healGeneration++
            }
        }
    }

    // key() is what makes already-resolved strings reload: Compose Resources caches the resolved
    // value per composition slot, so without a new key the tree keeps the strings it first read.
    key(locale.code, healGeneration) {
        CompositionLocalProvider(LocalLayoutDirection provides locale.direction) {
            content()
        }
    }
}

/**
 * Applies [languageTag] to the platform from *inside* composition, so the locale is set before any
 * `stringResource()` in this frame reads it. Android additionally re-runs this on every
 * configuration change and pushes the choice to the platform's per-app locale service.
 */
@Composable
internal expect fun applyLocale(languageTag: String)

/** The same mutation as [applyLocale], callable outside composition (drift repair, lifecycle callbacks). */
internal expect fun forceLocale(languageTag: String)

/**
 * The language subtag the resource resolver will actually see right now, normalised to the modern
 * ISO code so it compares directly against [AppLocale.code]. See [normalizeLanguageSubtag].
 */
internal expect fun currentPlatformLanguage(): String
