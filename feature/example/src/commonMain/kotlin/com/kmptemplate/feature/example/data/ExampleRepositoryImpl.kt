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
    } catch (e: IOException) {
        AppResult.Failure(DataError.Remote.NO_INTERNET)
    } catch (e: ResponseException) {
        AppResult.Failure(DataError.Remote.SERVER_ERROR)
    }
}
