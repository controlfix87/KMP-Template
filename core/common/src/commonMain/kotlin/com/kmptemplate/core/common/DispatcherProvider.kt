package com.kmptemplate.core.common

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

/**
 * Inject this instead of referencing [Dispatchers] directly anywhere reachable
 * from commonMain. A test provides a [DispatcherProvider] backed entirely by a
 * single `TestDispatcher` (see FakeExampleRepository / ExampleViewModelTest)
 * so coroutine-based code under test is deterministic without touching
 * `Dispatchers.setMain`.
 */
interface DispatcherProvider {
    val main: CoroutineDispatcher
    val io: CoroutineDispatcher
    val default: CoroutineDispatcher
}

object DefaultDispatcherProvider : DispatcherProvider {
    override val main: CoroutineDispatcher = Dispatchers.Main
    override val io: CoroutineDispatcher = Dispatchers.IO
    override val default: CoroutineDispatcher = Dispatchers.Default
}
