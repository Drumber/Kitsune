package io.github.drumber.kitsune.data.source.network.comment.model

import com.github.jasminb.jsonapi.annotations.Id
import com.github.jasminb.jsonapi.annotations.Relationship
import com.github.jasminb.jsonapi.annotations.Type
import io.github.drumber.kitsune.data.source.network.user.model.NetworkUser

@Type("commentLikes")
data class NetworkCommentLike(
    @Id
    val id: String?,

    @Relationship("comment")
    val comment: NetworkComment? = null,
    @Relationship("user")
    val user: NetworkUser? = null
)
