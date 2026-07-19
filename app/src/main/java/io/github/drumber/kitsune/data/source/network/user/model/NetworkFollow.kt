package io.github.drumber.kitsune.data.source.network.user.model

import com.github.jasminb.jsonapi.annotations.Id
import com.github.jasminb.jsonapi.annotations.Relationship
import com.github.jasminb.jsonapi.annotations.Type

@Type("follows")
data class NetworkFollow(
    @Id
    val id: String? = null,

    @Relationship("follower")
    val follower: NetworkUser? = null,
    @Relationship("followed")
    val followed: NetworkUser? = null
)
