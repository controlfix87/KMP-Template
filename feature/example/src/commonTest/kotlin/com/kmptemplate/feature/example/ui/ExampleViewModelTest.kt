package com.kmptemplate.feature.example.ui

import app.cash.turbine.test
import assertk.assertThat
import assertk.assertions.hasSize
import assertk.assertions.isEqualTo
import assertk.assertions.isFalse
import assertk.assertions.isNull
import com.kmptemplate.core.common.AppResult
import com.kmptemplate.core.common.DataError
import com.kmptemplate.core.model.ExampleItem
import com.kmptemplate.feature.example.testutil.FakeExampleRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test

/**
 * `ExampleViewModel` launches on `viewModelScope`, which resolves to
 * `Dispatchers.Main` -- unavailable on a plain JVM/commonTest run unless a
 * test dispatcher is installed as Main first. `UnconfinedTestDispatcher`
 * (rather than the default `StandardTestDispatcher`) runs launched coroutines
 * eagerly, so a `viewModel.state.test { ... }` block sees the post-`init`
 * state without needing an explicit `advanceUntilIdle()` in every test.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class ExampleViewModelTest {

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `loads items on init and clears loading`() = runTest {
        val repository = FakeExampleRepository().apply {
            refreshItems = listOf(ExampleItem(id = "1", title = "First"))
        }
        val viewModel = ExampleViewModel(repository)

        viewModel.state.test {
            val loaded = expectMostRecentItem()
            assertThat(loaded.isLoading).isFalse()
            assertThat(loaded.items).hasSize(1)
            assertThat(loaded.error).isNull()
        }
        assertThat(repository.calls).isEqualTo(listOf("refresh()"))
    }

    @Test
    fun `surfaces a DataError instead of throwing when refresh fails`() = runTest {
        val repository = FakeExampleRepository().apply {
            refreshResult = AppResult.Failure(DataError.Remote.NO_INTERNET)
        }
        val viewModel = ExampleViewModel(repository)

        viewModel.state.test {
            val loaded = expectMostRecentItem()
            assertThat(loaded.error).isEqualTo(DataError.Remote.NO_INTERNET)
            assertThat(loaded.isLoading).isFalse()
        }
    }

    @Test
    fun `Retry action calls refresh again`() = runTest {
        val repository = FakeExampleRepository()
        val viewModel = ExampleViewModel(repository)

        viewModel.onAction(ExampleAction.Retry)

        assertThat(repository.calls).isEqualTo(listOf("refresh()", "refresh()"))
    }

    @Test
    fun `ItemClicked emits a NavigateToDetail event`() = runTest {
        val viewModel = ExampleViewModel(FakeExampleRepository())

        viewModel.events.test {
            viewModel.onAction(ExampleAction.ItemClicked(itemId = "42"))
            assertThat(awaitItem()).isEqualTo(ExampleEvent.NavigateToDetail(itemId = "42"))
        }
    }
}
