package io.github.drumber.kitsune.data.source.jsonapi.media.model.production

import com.github.jasminb.jsonapi.annotations.Id
import com.github.jasminb.jsonapi.annotations.Relationship
import com.github.jasminb.jsonapi.annotations.Type

@Type("animeProductions")
data class NetworkAnimeProduction(
    @Id
    val id: String?,
    val role: NetworkAnimeProductionRole?,

    @Relationship("producer")
    val producer: NetworkProducer?
)
