package com.cmptemplate.feature.example.data

import com.cmptemplate.core.common.AppResult
import com.cmptemplate.core.common.DataError
import com.cmptemplate.core.common.EmptyResult
import com.cmptemplate.core.model.ExampleItem
import com.cmptemplate.feature.example.domain.ExampleRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

/** Offline sample data. Replace this binding when adding the first real feature. */
class DemoExampleRepository : ExampleRepository {
    private val items = MutableStateFlow(List(30) { index ->
        ExampleItem(id = (index + 1).toString(), title = "Sample ${index + 1}")
    })
    override fun observeItems() = items.asStateFlow()
    override suspend fun refresh(): EmptyResult<DataError.Remote> = AppResult.Success(Unit)
}
