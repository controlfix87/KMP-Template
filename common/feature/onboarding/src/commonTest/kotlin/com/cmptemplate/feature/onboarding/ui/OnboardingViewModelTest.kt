package com.cmptemplate.feature.onboarding.ui

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.cmptemplate.core.datastore.InMemoryUserPreferences
import com.cmptemplate.core.designsystem.i18n.AppLocale
import com.cmptemplate.core.designsystem.i18n.LocaleManager
import com.cmptemplate.core.designsystem.i18n.LocaleStore
import com.cmptemplate.core.model.ThemeMode
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

private class MemoryLocaleStore : LocaleStore {
    var code: String? = null
    override fun read() = code
    override fun write(code: String) { this.code = code }
}

@OptIn(ExperimentalCoroutinesApi::class)
class OnboardingViewModelTest {
    private val stores = mutableListOf<ViewModelStore>()
    @BeforeTest fun setUp() { Dispatchers.setMain(UnconfinedTestDispatcher()) }
    @AfterTest fun tearDown() { stores.forEach { it.clear() }; stores.clear(); Dispatchers.resetMain() }

    private fun TestScope.model(
        prefs: InMemoryUserPreferences = InMemoryUserPreferences(),
        saved: SavedStateHandle = SavedStateHandle(),
        locale: LocaleManager = LocaleManager(MemoryLocaleStore()),
    ): OnboardingViewModel {
        val store = ViewModelStore().also { stores.add(it) }
        val vm = ViewModelProvider.create(store, viewModelFactory { initializer { OnboardingViewModel(prefs, locale, saved) } })[OnboardingViewModel::class]
        // Keep the WhileSubscribed state live, as the screen does.
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { vm.state.collect {} }
        return vm
    }

    @Test fun `walks forward and back within bounds`() = runTest {
        val vm = model()
        vm.onAction(OnboardingAction.Back)
        assertEquals(OnboardingStep.WELCOME, vm.state.value.step)
        repeat(10) { vm.onAction(OnboardingAction.Next) }
        assertEquals(OnboardingStep.DONE, vm.state.value.step)
        assertTrue(vm.state.value.isLast)
        vm.onAction(OnboardingAction.Back)
        assertEquals(OnboardingStep.APP, vm.state.value.step)
    }

    @Test fun `the progress bar jumps back only, never forward`() = runTest {
        val vm = model()
        vm.onAction(OnboardingAction.GoTo(2))
        assertEquals(OnboardingStep.WELCOME, vm.state.value.step)
        vm.onAction(OnboardingAction.Next); vm.onAction(OnboardingAction.Next)
        vm.onAction(OnboardingAction.GoTo(0))
        assertEquals(OnboardingStep.WELCOME, vm.state.value.step)
    }

    @Test fun `skip stores done before the screen is told to leave`() = runTest {
        val prefs = InMemoryUserPreferences()
        val vm = model(prefs)
        assertFalse(vm.state.value.finished)
        vm.onAction(OnboardingAction.Skip)
        assertTrue(prefs.settings.value.onboardingDone)
        assertTrue(vm.state.value.finished)
    }

    @Test fun `language and theme choices reach the locale manager and DataStore`() = runTest {
        val prefs = InMemoryUserPreferences()
        val vm = model(prefs)
        vm.onAction(OnboardingAction.SetLocale(AppLocale.Hebrew))
        vm.onAction(OnboardingAction.SetTheme(ThemeMode.DARK))
        assertEquals(AppLocale.Hebrew, vm.state.value.locale)
        assertEquals(ThemeMode.DARK, prefs.settings.value.themeMode)
        assertEquals(ThemeMode.DARK, vm.state.value.theme)
    }

    @Test fun `the step survives recreation through saved state`() = runTest {
        val saved = SavedStateHandle()
        model(saved = saved).onAction(OnboardingAction.Next)
        assertEquals(OnboardingStep.APP, model(saved = saved).state.value.step)
    }
}
