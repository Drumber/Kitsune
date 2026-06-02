package io.github.drumber.kitsune.data.source.network.feed.model

import com.fasterxml.jackson.annotation.JsonProperty
import com.github.jasminb.jsonapi.annotations.Id
import com.github.jasminb.jsonapi.annotations.Relationship
import com.github.jasminb.jsonapi.annotations.Type
import io.github.drumber.kitsune.data.source.network.user.model.NetworkUser

/**
 * Request body model for creating an upload. The [content] attribute is the base64 data URI of the
 * image and is only sent during serialization; the server responds with an upload whose `content`
 * is an image object, which is ignored here (we only read back the [id]).
 */
@Type("uploads")
data class NetworkUploadRequest(
    @Id
    val id: String? = null,

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    val content: String? = null,

    val uploadOrder: Int? = null,

    @Relationship("user")
    val user: NetworkUser? = null
)
