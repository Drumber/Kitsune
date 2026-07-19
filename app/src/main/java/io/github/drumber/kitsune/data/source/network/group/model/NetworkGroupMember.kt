package io.github.drumber.kitsune.data.source.network.group.model

import com.github.jasminb.jsonapi.annotations.Id
import com.github.jasminb.jsonapi.annotations.Relationship
import com.github.jasminb.jsonapi.annotations.Type
import io.github.drumber.kitsune.data.source.network.user.model.NetworkUser

@Type("groupMembers")
data class NetworkGroupMember(
    @Id
    val id: String?,

    val createdAt: String? = null,
    val updatedAt: String? = null,
    val unreadCount: Int? = null,

    @Relationship("group")
    val group: NetworkGroup? = null,

    @Relationship("user")
    val user: NetworkUser? = null
)
