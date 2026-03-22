package io.github.drumber.kitsune.data.source.jsonapi.user.model.profilelinks

import com.github.jasminb.jsonapi.annotations.Id
import com.github.jasminb.jsonapi.annotations.Type

@Type("profileLinkSites")
data class NetworkProfileLinkSite(
    @Id
    val id: String?,
    val name: String?
)
