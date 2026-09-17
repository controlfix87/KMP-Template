package com.kmptemplate.feature.example.ui

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import app.cash.turbine.test
import com.kmptemplate.core.common.AppResult
import com.kmptemplate.core.common.DataError
import com.kmptemplate.core.model.ExampleItem
import com.kmptemplate.feature.example.testutil.FakeExampleRepository
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import kotlin.test.*

@OptIn(ExperimentalCoroutinesApi::class)
class ExampleViewModelTest {
    private val stores = mutableListOf<ViewModelStore>()
    @BeforeTest fun setUp() { Dispatchers.setMain(UnconfinedTestDispatcher()) }
    @AfterTest fun tearDown() { stores.forEach { it.clear() }; stores.clear(); Dispatchers.resetMain() }
    private fun model(repo: FakeExampleRepository, saved: SavedStateHandle = SavedStateHandle()): ExampleViewModel {
        val store = ViewModelStore().also { stores.add(it) }
        return ViewModelProvider.create(store, viewModelFactory { initializer { ExampleViewModel(repo, saved) } })[ExampleViewModel::class]
    }
    @Test fun `loads only on subscription and does not repeat after rotation timeout`() = runTest {
        val repo = FakeExampleRepository(listOf(ExampleItem("1", "First")))
        val vm = model(repo)
        assertTrue(repo.calls.isEmpty())
        vm.state.test { assertEquals(1, expectMostRecentItem().items.size) }
        advanceTimeBy(6_000)
        vm.state.test { assertFalse(expectMostRecentItem().isLoading) }
        assertEquals(listOf("refresh()"), repo.calls)
    }
    @Test fun `retry after failure clears error and loads items`() = runTest {
        val repo = FakeExampleRepository().apply { refreshResult = AppResult.Failure(DataError.Remote.NO_INTERNET) }
        val vm = model(repo)
        vm.state.test {
            assertEquals(DataError.Remote.NO_INTERNET, expectMostRecentItem().error)
            repo.refreshResult = AppResult.Success(Unit)
            vm.onAction(ExampleAction.Retry)
            assertNull(expectMostRecentItem().error)
        }
        assertEquals(2, repo.calls.size)
    }
    @Test fun `rapid retry while request is in flight does not duplicate work`() = runTest {
        val repo = FakeExampleRepository().apply { refreshGate = CompletableDeferred() }
        val vm = model(repo)
        vm.state.test {
            repeat(10) { vm.onAction(ExampleAction.Retry) }
            assertEquals(1, repo.calls.size)
            repo.refreshGate!!.complete(Unit)
            assertFalse(expectMostRecentItem().isLoading)
        }
    }
    @Test fun `query restores through saved state and filters case insensitively`() = runTest {
        val repo = FakeExampleRepository(listOf(ExampleItem("1", "First"), ExampleItem("2", "Second")))
        val saved = SavedStateHandle(mapOf("query" to "FIRST"))
        val vm = model(repo, saved)
        vm.state.test {
            assertEquals(listOf("1"), expectMostRecentItem().items.map { it.id })
            vm.onAction(ExampleAction.QueryChanged("Second"))
            assertEquals("Second", saved.get<String>("query"))
            assertEquals(listOf("2"), expectMostRecentItem().items.map { it.id })
        }
        model(repo, SavedStateHandle(mapOf("query" to saved.get<String>("query")))).state.test {
            assertEquals("Second", expectMostRecentItem().query)
        }
    }
}
