package io.github.drumber.kitsune.data.source.network.feed.model

import com.github.jasminb.jsonapi.annotations.Id
import com.github.jasminb.jsonapi.annotations.Relationship
import com.github.jasminb.jsonapi.annotations.Type
import io.github.drumber.kitsune.data.source.network.media.model.NetworkMedia
import io.github.drumber.kitsune.data.source.network.user.model.NetworkUser

@Type("posts")
data class NetworkPost(
    @Id
    val id: String?,

    val createdAt: String? = null,
    val updatedAt: String? = null,

    val content: String? = null,
    val contentFormatted: String? = null,

    val spoiler: Boolean? = null,
    val nsfw: Boolean? = null,

    val commentsCount: Int? = null,
    val topLevelCommentsCount: Int? = null,
    val postLikesCount: Int? = null,

    @Relationship("user")
    val user: NetworkUser? = null,
    @Relationship("media")
    val media: NetworkMedia? = null
)
