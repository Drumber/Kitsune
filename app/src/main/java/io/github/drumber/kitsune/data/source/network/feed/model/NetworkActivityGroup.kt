package io.github.drumber.kitsune.data.source.network.feed.model

import com.github.jasminb.jsonapi.annotations.Id
import com.github.jasminb.jsonapi.annotations.Relationship
import com.github.jasminb.jsonapi.annotations.Type

@Type("activityGroups")
data class NetworkActivityGroup(
    @Id
    val id: String?,

    val group: String? = null,
    val activityCount: Int? = null,
    val actorCount: Int? = null,
    val createdAt: String? = null,
    val updatedAt: String? = null,

    @Relationship("activities")
    val activities: List<NetworkActivity>? = null
)
