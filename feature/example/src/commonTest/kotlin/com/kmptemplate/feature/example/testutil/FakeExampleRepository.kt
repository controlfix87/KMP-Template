package com.kmptemplate.feature.example.testutil

import com.kmptemplate.core.common.AppResult
import com.kmptemplate.core.common.DataError
import com.kmptemplate.core.common.EmptyResult
import com.kmptemplate.core.model.ExampleItem
import com.kmptemplate.feature.example.domain.ExampleRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Hand-written test double, not a mock of [ExampleRepository] -- it drives a
 * real [MutableStateFlow] the way the production implementation does, and
 * [calls] records what the ViewModel actually invoked so a test can assert
 * "refresh was called once on Retry" instead of just "the end state looks
 * right", which would miss a ViewModel that reloads on every recomposition.
 */
class FakeExampleRepository(
    initialItems: List<ExampleItem> = emptyList(),
) : ExampleRepository {

    private val itemsFlow = MutableStateFlow(initialItems)

    /** What [refresh] returns and, on success, replaces [observeItems] with -- set from a test. */
    var refreshResult: EmptyResult<DataError.Remote> = AppResult.Success(Unit)
    var refreshItems: List<ExampleItem> = initialItems

    val calls = mutableListOf<String>()

    override fun observeItems() = itemsFlow.asStateFlow()

    override suspend fun refresh(): EmptyResult<DataError.Remote> {
        calls += "refresh()"
        val result = refreshResult
        if (result is AppResult.Success) {
            itemsFlow.value = refreshItems
        }
        return result
    }
}
