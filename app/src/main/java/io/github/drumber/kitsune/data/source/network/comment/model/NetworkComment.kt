package io.github.drumber.kitsune.data.source.network.comment.model

import com.github.jasminb.jsonapi.annotations.Id
import com.github.jasminb.jsonapi.annotations.Relationship
import com.github.jasminb.jsonapi.annotations.Type
import io.github.drumber.kitsune.data.source.network.feed.model.NetworkEmbed
import io.github.drumber.kitsune.data.source.network.feed.model.NetworkFeedSubject
import io.github.drumber.kitsune.data.source.network.feed.model.NetworkPost
import io.github.drumber.kitsune.data.source.network.feed.model.NetworkUpload
import io.github.drumber.kitsune.data.source.network.user.model.NetworkUser

@Type("comments")
data class NetworkComment(
    @Id
    val id: String?,

    val content: String? = null,
    val contentFormatted: String? = null,

    val likesCount: Int? = null,
    val repliesCount: Int? = null,

    val createdAt: String? = null,
    val updatedAt: String? = null,
    val editedAt: String? = null,

    val embed: NetworkEmbed? = null,

    @Relationship("user")
    val user: NetworkUser? = null,
    @Relationship("post")
    val post: NetworkPost? = null,
    @Relationship("parent")
    val parent: NetworkComment? = null,
    @Relationship("uploads")
    val uploads: List<NetworkUpload>? = null
) : NetworkFeedSubject
