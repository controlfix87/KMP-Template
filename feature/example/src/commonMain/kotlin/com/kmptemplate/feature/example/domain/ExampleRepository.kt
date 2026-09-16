package com.kmptemplate.feature.example.domain

import com.kmptemplate.core.common.DataError
import com.kmptemplate.core.common.EmptyResult
import com.kmptemplate.core.model.ExampleItem
import kotlinx.coroutines.flow.Flow

/**
 * Deliberately an interface, not a class. Production code gets the real
 * implementation from Koin (see di/ExampleModule.kt); tests substitute a
 * hand-written fake (testutil/FakeExampleRepository.kt) -- never a mock of
 * this interface. A fake that tracks calls and drives its own StateFlow
 * catches ViewModel wiring bugs a relaxed mock would silently hide.
 */
interface ExampleRepository {
    /** Emits the current cached items immediately, then again after every [refresh]. */
    fun observeItems(): Flow<List<ExampleItem>>

    suspend fun refresh(): EmptyResult<DataError.Remote>
}
