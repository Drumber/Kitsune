package io.github.drumber.kitsune.data.source.network.mapping.model

import com.github.jasminb.jsonapi.annotations.Id
import com.github.jasminb.jsonapi.annotations.Type

@Type("mappings")
data class NetworkMapping(
    @Id
    val id: String?,
    val externalSite: String?,
    val externalId: String?
)
