package io.github.drumber.kitsune.data.source.network.feed.model

import com.github.jasminb.jsonapi.annotations.Id
import com.github.jasminb.jsonapi.annotations.Relationship
import com.github.jasminb.jsonapi.annotations.Type
import io.github.drumber.kitsune.data.source.network.user.model.NetworkUser

@Type("activities")
data class NetworkActivity(
    @Id
    val id: String?,

    val foreignId: String? = null,
    val verb: String? = null,
    val time: String? = null,
    val createdAt: String? = null,
    val updatedAt: String? = null,

    @Relationship("subject")
    val subject: NetworkPost? = null,
    @Relationship("actor")
    val actor: NetworkUser? = null
)
