package io.github.drumber.kitsune.data.model.user.profilelinks

import io.github.drumber.kitsune.data.model.user.User

data class ProfileLink(
    val id: String,
    val url: String?,
    val profileLinkSite: ProfileLinkSite?,
    val user: User?
)
