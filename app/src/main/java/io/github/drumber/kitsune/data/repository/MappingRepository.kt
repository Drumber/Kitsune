package io.github.drumber.kitsune.data.repository

import io.github.drumber.kitsune.data.mapper.MappingMapper.toMapping
import io.github.drumber.kitsune.data.presentation.model.mapping.Mapping
import io.github.drumber.kitsune.data.source.jsonapi.mapping.MappingNetworkDataSource
import io.github.drumber.kitsune.data.model.Filter

class MappingRepository(
    private val mappingNetworkDataSource: MappingNetworkDataSource
) {

    suspend fun getAnimeMappings(animeId: String, filter: Filter = Filter()): List<Mapping>? {
        return mappingNetworkDataSource.getAnimeMappings(animeId, filter)?.map { it.toMapping() }
    }

    suspend fun getMangaMappings(mangaId: String, filter: Filter = Filter()): List<Mapping>? {
        return mappingNetworkDataSource.getMangaMappings(mangaId, filter)?.map { it.toMapping() }
    }
}