package io.github.drumber.kitsune.data.source.local.user.model

data class LocalProfileLink(
    val id: String,
    val url: String?,
    val profileLinkSiteName: LocalProfileLinkSite?
)
