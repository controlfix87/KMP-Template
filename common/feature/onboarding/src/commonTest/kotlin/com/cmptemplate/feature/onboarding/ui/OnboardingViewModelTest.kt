package com.cmptemplate.feature.onboarding.ui

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.cmptemplate.core.designsystem.settings.InMemoryPreferences
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class OnboardingViewModelTest {
    private val stores = mutableListOf<ViewModelStore>()
    @BeforeTest fun setUp() { Dispatchers.setMain(UnconfinedTestDispatcher()) }
    @AfterTest fun tearDown() { stores.forEach { it.clear() }; stores.clear(); Dispatchers.resetMain() }

    private fun model(prefs: InMemoryPreferences, saved: SavedStateHandle = SavedStateHandle()): OnboardingViewModel {
        val store = ViewModelStore().also { stores.add(it) }
        return ViewModelProvider.create(store, viewModelFactory { initializer { OnboardingViewModel(prefs, saved) } })[OnboardingViewModel::class]
    }

    @Test fun `walks forward and back within bounds`() {
        val vm = model(InMemoryPreferences())
        vm.onAction(OnboardingAction.Back)
        assertEquals(OnboardingStep.WELCOME, vm.state.value.step)
        repeat(10) { vm.onAction(OnboardingAction.Next) }
        assertEquals(OnboardingStep.DONE, vm.state.value.step)
        assertTrue(vm.state.value.isLast)
        vm.onAction(OnboardingAction.Back)
        assertEquals(OnboardingStep.APP, vm.state.value.step)
    }

    @Test fun `the progress bar jumps back only, never forward`() {
        val vm = model(InMemoryPreferences())
        vm.onAction(OnboardingAction.GoTo(2))
        assertEquals(OnboardingStep.WELCOME, vm.state.value.step)
        vm.onAction(OnboardingAction.Next); vm.onAction(OnboardingAction.Next)
        vm.onAction(OnboardingAction.GoTo(0))
        assertEquals(OnboardingStep.WELCOME, vm.state.value.step)
    }

    @Test fun `skip and finish both mark setup done; reset brings it back`() {
        val prefs = InMemoryPreferences()
        val vm = model(prefs)
        assertFalse(OnboardingViewModel.isDone(prefs))
        assertTrue(vm.onAction(OnboardingAction.Skip))
        assertTrue(OnboardingViewModel.isDone(prefs))
        OnboardingViewModel.reset(prefs)
        assertFalse(OnboardingViewModel.isDone(prefs))
        assertFalse(vm.onAction(OnboardingAction.Next))
        assertTrue(model(prefs).onAction(OnboardingAction.Finish))
        assertTrue(OnboardingViewModel.isDone(prefs))
    }

    @Test fun `the step survives recreation through saved state`() {
        val saved = SavedStateHandle()
        model(InMemoryPreferences(), saved).onAction(OnboardingAction.Next)
        assertEquals(OnboardingStep.APP, model(InMemoryPreferences(), saved).state.value.step)
    }
}
