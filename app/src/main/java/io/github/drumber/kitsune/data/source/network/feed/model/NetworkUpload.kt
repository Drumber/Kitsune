package io.github.drumber.kitsune.data.source.network.feed.model

import com.github.jasminb.jsonapi.annotations.Id
import com.github.jasminb.jsonapi.annotations.Relationship
import com.github.jasminb.jsonapi.annotations.Type
import io.github.drumber.kitsune.data.source.network.NetworkImage
import io.github.drumber.kitsune.data.source.network.user.model.NetworkUser

@Type("uploads")
data class NetworkUpload(
    @Id
    val id: String?,

    val content: NetworkImage? = null,
    val contentType: String? = null,
    val uploadOrder: Int? = null,

    @Relationship("user")
    val user: NetworkUser? = null
)
