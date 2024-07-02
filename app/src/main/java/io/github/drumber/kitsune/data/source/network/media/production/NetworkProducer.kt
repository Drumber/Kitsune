package io.github.drumber.kitsune.data.source.network.media.production

import com.github.jasminb.jsonapi.annotations.Id
import com.github.jasminb.jsonapi.annotations.Type

@Type("producers")
data class NetworkProducer(
    @Id
    val id: String?,
    val slug: String?,
    val name: String?
)
