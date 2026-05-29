package io.github.drumber.kitsune.data.source.network.reaction.model

import com.github.jasminb.jsonapi.annotations.Id
import com.github.jasminb.jsonapi.annotations.Relationship
import com.github.jasminb.jsonapi.annotations.Type
import io.github.drumber.kitsune.data.source.network.media.model.NetworkAnime
import io.github.drumber.kitsune.data.source.network.media.model.NetworkManga
import io.github.drumber.kitsune.data.source.network.user.model.NetworkUser

@Type("mediaReactions")
data class NetworkMediaReaction(
    @Id
    val id: String?,

    val reaction: String? = null,
    val content: String? = null,
    val contentFormatted: String? = null,

    val upVotesCount: Int? = null,
    val likesCount: Int? = null,

    val createdAt: String? = null,
    val updatedAt: String? = null,

    @Relationship("user")
    val user: NetworkUser? = null,
    @Relationship("anime")
    val anime: NetworkAnime? = null,
    @Relationship("manga")
    val manga: NetworkManga? = null
)
