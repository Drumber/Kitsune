package io.github.drumber.kitsune.data.source.jsonapi.user.model.profilelinks

import com.github.jasminb.jsonapi.annotations.Id
import com.github.jasminb.jsonapi.annotations.Relationship
import com.github.jasminb.jsonapi.annotations.Type
import io.github.drumber.kitsune.data.source.jsonapi.user.model.NetworkUser

@Type("profileLinks")
data class NetworkProfileLink(
    @Id
    val id: String?,
    val url: String?,

    @Relationship("profileLinkSite")
    val profileLinkSite: NetworkProfileLinkSite?,
    @Relationship("user")
    val user: NetworkUser?
) {
    companion object {
        fun empty() = NetworkProfileLink(
            null,
            null,
            null,
            null
        )
    }
}
