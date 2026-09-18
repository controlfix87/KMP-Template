package com.kmptemplate.feature.example.data

import com.kmptemplate.core.common.AppResult
import com.kmptemplate.core.common.DataError
import com.kmptemplate.core.common.EmptyResult
import com.kmptemplate.core.model.ExampleItem
import com.kmptemplate.feature.example.domain.ExampleRepository
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.plugins.ResponseException
import io.ktor.client.plugins.HttpRequestTimeoutException
import kotlinx.serialization.SerializationException
import kotlin.coroutines.cancellation.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.io.IOException
import kotlinx.serialization.Serializable

@Serializable
private data class ExampleItemDto(val id: String, val title: String)

private fun ExampleItemDto.toDomain() = ExampleItem(id = id, title = title)

/**
 * Replace `"example/items"` with a real endpoint, or swap this whole class
 * for a database- or DataStore-backed implementation -- [ExampleRepository]
 * is the seam, this is just one implementation of it.
 */
class ExampleRepositoryImpl(
    private val httpClient: HttpClient,
) : ExampleRepository {

    private val items = MutableStateFlow<List<ExampleItem>>(emptyList())

    override fun observeItems() = items.asStateFlow()

    override suspend fun refresh(): EmptyResult<DataError.Remote> = try {
        val dtos: List<ExampleItemDto> = httpClient.get("example/items").body()
        items.value = dtos.map { it.toDomain() }
        AppResult.Success(Unit)
    } catch (e: CancellationException) {
        throw e
    } catch (e: HttpRequestTimeoutException) {
        AppResult.Failure(DataError.Remote.REQUEST_TIMEOUT)
    } catch (e: IOException) {
        AppResult.Failure(DataError.Remote.NO_INTERNET)
    } catch (e: ResponseException) {
        AppResult.Failure(when (e.response.status.value) {
            401, 403 -> DataError.Remote.UNAUTHORIZED
            404 -> DataError.Remote.NOT_FOUND
            408 -> DataError.Remote.REQUEST_TIMEOUT
            in 500..599 -> DataError.Remote.SERVER_ERROR
            else -> DataError.Remote.UNKNOWN
        })
    } catch (e: SerializationException) {
        AppResult.Failure(DataError.Remote.SERIALIZATION)
    } catch (e: io.ktor.serialization.ContentConvertException) {
        AppResult.Failure(DataError.Remote.SERIALIZATION)
    }
}
