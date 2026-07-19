package io.github.drumber.kitsune.data.source.network.feed.model

import com.github.jasminb.jsonapi.annotations.Id
import com.github.jasminb.jsonapi.annotations.Relationship
import com.github.jasminb.jsonapi.annotations.Type
import io.github.drumber.kitsune.data.source.network.user.model.NetworkUser

@Type("postLikes")
data class NetworkPostLike(
    @Id
    val id: String?,

    @Relationship("post")
    val post: NetworkPost? = null,
    @Relationship("user")
    val user: NetworkUser? = null
)
