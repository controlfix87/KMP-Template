package com.kmptemplate.core.designsystem.i18n

import androidx.compose.ui.unit.LayoutDirection
import assertk.assertThat
import assertk.assertions.containsExactlyInAnyOrder
import assertk.assertions.isEqualTo
import assertk.assertions.isNotNull
import assertk.assertions.isNull
import kotlin.test.Test

class AppLocaleTest {

    private class FakeLocaleStore(initial: String? = null) : LocaleStore {
        var stored: String? = initial
            private set
        var writes = 0
            private set

        override fun read(): String? = stored

        override fun write(code: String) {
            stored = code
            writes++
        }
    }

    @Test
    fun shipsTheFourDefaultLanguages() {
        // The template's promise is English, Hebrew, Russian and French out of the box. Each also
        // needs a values-<code>/strings.xml with the full key set -- scripts/check_project.py
        // enforces that half, this enforces the enum half.
        assertThat(AppLocale.entries.map { it.code })
            .containsExactlyInAnyOrder("en", "he", "ru", "fr")
    }

    @Test
    fun defaultLocaleCodeResolvesToAShippedLanguage() {
        assertThat(AppLocale.ofCode(DEFAULT_LOCALE_CODE)).isNotNull()
        assertThat(AppLocale.Default.code).isEqualTo(DEFAULT_LOCALE_CODE)
    }

    @Test
    fun everyCodeRoundTrips() {
        for (locale in AppLocale.entries) {
            assertThat(AppLocale.ofCode(locale.code)).isEqualTo(locale)
        }
    }

    @Test
    fun unknownCodeDoesNotResolve() {
        assertThat(AppLocale.ofCode("zz")).isNull()
        assertThat(AppLocale.ofCode(null)).isNull()
    }

    @Test
    fun hebrewIsTheOnlyRightToLeftLanguage() {
        for (locale in AppLocale.entries) {
            val expected = if (locale == AppLocale.Hebrew) LayoutDirection.Rtl else LayoutDirection.Ltr
            assertThat(locale.direction).isEqualTo(expected)
        }
    }

    @Test
    fun legacyIsoCodesNormaliseToTheModernOnes() {
        // Android's Locale.getLanguage() still reports Hebrew as "iw". AppLocaleProvider compares
        // that reading against AppLocale.code ("he") to decide whether the platform locale drifted;
        // without this mapping every check would report a false drift and rebuild the whole tree.
        assertThat(normalizeLanguageSubtag("iw")).isEqualTo("he")
        assertThat(normalizeLanguageSubtag("IW")).isEqualTo("he")
        assertThat(normalizeLanguageSubtag("in")).isEqualTo("id")
        assertThat(normalizeLanguageSubtag("ji")).isEqualTo("yi")
    }

    @Test
    fun everyShippedCodeSurvivesNormalisation() {
        // The drift check compares normalizeLanguageSubtag(platform) against AppLocale.code, so no
        // shipped code may itself be rewritten by the normaliser.
        for (locale in AppLocale.entries) {
            assertThat(normalizeLanguageSubtag(locale.code)).isEqualTo(locale.code)
        }
    }

    @Test
    fun missingPreferenceResolvesToTheDefaultAndIsPersisted() {
        // Persisting matters as much as resolving: Android's attachBaseContext reads this key
        // directly, before this class exists, and an unwritten default leaves it on the device
        // language until the first configuration change.
        val store = FakeLocaleStore()

        assertThat(LocaleManager(store).locale.value).isEqualTo(AppLocale.Default)
        assertThat(store.stored).isEqualTo(DEFAULT_LOCALE_CODE)
    }

    @Test
    fun unrecognisedPreferenceIsRepairedToTheDefault() {
        val store = FakeLocaleStore("klingon")

        assertThat(LocaleManager(store).locale.value).isEqualTo(AppLocale.Default)
        assertThat(store.stored).isEqualTo(DEFAULT_LOCALE_CODE)
    }

    @Test
    fun aStoredLanguageIsLoadedAsIsAndNotRewritten() {
        val store = FakeLocaleStore(AppLocale.Russian.code)

        assertThat(LocaleManager(store).locale.value).isEqualTo(AppLocale.Russian)
        assertThat(store.writes).isEqualTo(0)
    }

    @Test
    fun settingALanguagePersistsItAndPublishesIt() {
        val store = FakeLocaleStore()
        val manager = LocaleManager(store)

        manager.setLocale(AppLocale.French)

        assertThat(manager.locale.value).isEqualTo(AppLocale.French)
        assertThat(store.stored).isEqualTo(AppLocale.French.code)
    }
}
