package com.kmptemplate.core.common

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isFalse
import assertk.assertions.isNull
import assertk.assertions.isTrue
import kotlin.test.Test

class AppResultTest {

    @Test
    fun `map transforms success and leaves failure untouched`() {
        val success: AppResult<Int, DataError.Remote> = AppResult.Success(1)
        val failure: AppResult<Int, DataError.Remote> = AppResult.Failure(DataError.Remote.UNKNOWN)

        assertThat(success.map { it + 1 }).isEqualTo(AppResult.Success(2))
        assertThat(failure.map { it + 1 }).isEqualTo(failure)
    }

    @Test
    fun `isSuccess and getOrNull agree with the variant`() {
        val success: AppResult<Int, DataError.Remote> = AppResult.Success(42)
        val failure: AppResult<Int, DataError.Remote> = AppResult.Failure(DataError.Remote.NOT_FOUND)

        assertThat(success.isSuccess).isTrue()
        assertThat(success.getOrNull()).isEqualTo(42)

        assertThat(failure.isSuccess).isFalse()
        assertThat(failure.getOrNull()).isNull()
        assertThat(failure.errorOrNull()).isEqualTo(DataError.Remote.NOT_FOUND)
    }
}
