package io.github.drumber.kitsune.data.source.jsonapi.media.model.production

import com.github.jasminb.jsonapi.annotations.Id
import com.github.jasminb.jsonapi.annotations.Type
import io.github.drumber.kitsune.data.common.Image

@Type("people")
data class NetworkPerson(
    @Id
    val id: String?,
    val name: String?,
    val description: String?,
    val image: Image?
)
