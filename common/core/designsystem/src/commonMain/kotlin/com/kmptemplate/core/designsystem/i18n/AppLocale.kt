package com.kmptemplate.core.designsystem.i18n

import androidx.compose.ui.unit.LayoutDirection

/**
 * The language a fresh install starts in, before the user has ever picked one.
 *
 * The template deliberately does **not** follow the device language. Following it sounds friendly
 * and behaves badly: the device language is re-applied to the process by the OS on events the app
 * does not control, so an app that treats it as an input ends up switching language on a rotation
 * or a background configuration update. Here the language has exactly one input — the picker — and
 * exactly one stored value. Change this constant if your product should start somewhere else.
 */
const val DEFAULT_LOCALE_CODE = "en"

/**
 * Every language this app ships. Adding one means: a new entry here, a matching
 * `composeResources/values-<code>/strings.xml` with the full key set, and nothing else — the
 * picker, the persistence and the platform plumbing are all driven off this enum.
 *
 * [code] doubles as the resource-directory suffix and the BCP-47 language tag handed to the
 * platform, so the two can never disagree (`AppLocaleTest` asserts it).
 */
enum class AppLocale(
    val code: String,
    val direction: LayoutDirection,
    /** The language's own name for itself. Never translated — see `values/strings.xml`. */
    val displayName: String,
    val flag: String,
) {
    English("en", LayoutDirection.Ltr, "English", "🇬🇧"),
    Hebrew("he", LayoutDirection.Rtl, "עברית", "🇮🇱"),
    Russian("ru", LayoutDirection.Ltr, "Русский", "🇷🇺"),
    French("fr", LayoutDirection.Ltr, "Français", "🇫🇷"),
    ;

    companion object {
        fun ofCode(code: String?): AppLocale? = entries.firstOrNull { it.code == code }

        val Default: AppLocale = entries.first { it.code == DEFAULT_LOCALE_CODE }
    }
}

/**
 * Java — and therefore Android — still reports Hebrew, Indonesian and Yiddish under their withdrawn
 * 1989 ISO 639 codes from `Locale.getLanguage()`. Everything here uses the modern codes, so a
 * platform reading is normalised through this before being compared against an [AppLocale.code].
 * Without it the drift check in [AppLocaleProvider] would report a false mismatch for Hebrew on
 * every single pass.
 */
internal fun normalizeLanguageSubtag(subtag: String): String = when (val s = subtag.lowercase()) {
    "iw" -> "he"
    "in" -> "id"
    "ji" -> "yi"
    else -> s
}
