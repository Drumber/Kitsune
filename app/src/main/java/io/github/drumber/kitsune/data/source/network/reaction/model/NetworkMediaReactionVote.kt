package io.github.drumber.kitsune.data.source.network.reaction.model

import com.github.jasminb.jsonapi.annotations.Id
import com.github.jasminb.jsonapi.annotations.Relationship
import com.github.jasminb.jsonapi.annotations.Type
import io.github.drumber.kitsune.data.source.network.user.model.NetworkUser

@Type("mediaReactionVotes")
data class NetworkMediaReactionVote(
    @Id
    val id: String?,

    @Relationship("mediaReaction")
    val mediaReaction: NetworkMediaReaction? = null,
    @Relationship("user")
    val user: NetworkUser? = null
)
