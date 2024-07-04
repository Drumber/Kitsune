package io.github.drumber.kitsune.data.mapper

import io.github.drumber.kitsune.data.presentation.model.mapping.Mapping
import io.github.drumber.kitsune.data.source.network.mapping.model.NetworkMapping

object MappingMapper {
    fun NetworkMapping.toMapping() = Mapping(
        id = id.require(),
        externalSite = externalSite,
        externalId = externalId
    )
}