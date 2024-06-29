package io.github.drumber.kitsune.data.source.network.user.model

import com.github.jasminb.jsonapi.annotations.Id
import com.github.jasminb.jsonapi.annotations.Relationship
import com.github.jasminb.jsonapi.annotations.Type
import io.github.drumber.kitsune.domain_old.model.infrastructure.user.User

@Type("profileLinks")
data class NetworkProfileLink(
    @Id
    val id: String?,
    val url: String?,

    @Relationship("profileLinkSite")
    val profileLinkSite: NetworkProfileLinkSite?,
    @Relationship("user")
    val user: User?
)
