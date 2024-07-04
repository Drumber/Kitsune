package io.github.drumber.kitsune.data.source.network.mapping

import io.github.drumber.kitsune.data.source.network.mapping.api.MappingApi
import io.github.drumber.kitsune.data.source.network.mapping.model.NetworkMapping
import io.github.drumber.kitsune.domain_old.service.Filter
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