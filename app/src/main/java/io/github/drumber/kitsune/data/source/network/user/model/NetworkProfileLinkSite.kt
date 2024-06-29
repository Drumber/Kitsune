package io.github.drumber.kitsune.data.source.network.user.model

import com.github.jasminb.jsonapi.annotations.Id
import com.github.jasminb.jsonapi.annotations.Type

@Type("profileLinkSites")
data class NetworkProfileLinkSite(
    @Id
    val id: String?,
    val name: String?
)
