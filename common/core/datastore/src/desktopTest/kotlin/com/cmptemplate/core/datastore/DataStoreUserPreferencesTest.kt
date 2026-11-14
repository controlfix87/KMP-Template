package com.cmptemplate.core.datastore

import com.cmptemplate.core.model.ThemeMode
import com.cmptemplate.core.model.UserSettings
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import java.io.File
import java.nio.file.Files
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals

/** Real DataStore on a temporary file (desktop JVM): defaults, writes, and corrupt-file recovery. */
class DataStoreUserPreferencesTest {
    private val dir: File = Files.createTempDirectory("prefs").toFile()
    @AfterTest fun clean() { dir.deleteRecursively() }

    @Test fun `defaults, then writes are read back`() = runTest {
        val repo = DataStoreUserPreferences(createPreferencesDataStore(File(dir, "a.preferences_pb").absolutePath))
        assertEquals(UserSettings(), repo.settings.first())
        repo.setThemeMode(ThemeMode.DARK)
        repo.setOnboardingDone(true)
        assertEquals(UserSettings(ThemeMode.DARK, onboardingDone = true), repo.settings.first())
    }

    @Test fun `a corrupt file falls back to defaults instead of crashing`() = runTest {
        val file = File(dir, "b.preferences_pb").apply { writeBytes(byteArrayOf(1, 2, 3, 4, 5)) }
        val repo = DataStoreUserPreferences(createPreferencesDataStore(file.absolutePath))
        assertEquals(UserSettings(), repo.settings.first())
        repo.setThemeMode(ThemeMode.LIGHT)
        assertEquals(ThemeMode.LIGHT, repo.settings.first().themeMode)
    }
}
