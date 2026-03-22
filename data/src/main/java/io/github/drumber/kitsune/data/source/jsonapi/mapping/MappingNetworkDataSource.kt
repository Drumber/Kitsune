package io.github.drumber.kitsune.data.source.jsonapi.mapping

import io.github.drumber.kitsune.data.model.Filter
import io.github.drumber.kitsune.data.source.jsonapi.mapping.api.MappingApi
import io.github.drumber.kitsune.data.source.jsonapi.mapping.model.NetworkMapping
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class MappingNetworkDataSource(
    private val mappingApi: MappingApi
) {

    suspend fun getAnimeMappings(animeId: String, filter: Filter): List<NetworkMapping>? {
        return withContext(Dispatchers.IO) {
            mappingApi.getAnimeMappings(animeId, filter.options).get()
        }
    }

    suspend fun getMangaMappings(mangaId: String, filter: Filter): List<NetworkMapping>? {
        return withContext(Dispatchers.IO) {
            mappingApi.getMangaMappings(mangaId, filter.options).get()
        }
    }
}