package io.github.drumber.kitsune.data.mapper

import io.github.drumber.kitsune.data.mapper.UserMapper.toUser
import io.github.drumber.kitsune.data.model.user.profilelinks.ProfileLink
import io.github.drumber.kitsune.data.model.user.profilelinks.ProfileLinkSite
import io.github.drumber.kitsune.data.source.jsonapi.user.model.profilelinks.NetworkProfileLink
import io.github.drumber.kitsune.data.source.jsonapi.user.model.profilelinks.NetworkProfileLinkSite

object ProfileLinksMapper {
    fun NetworkProfileLink.toProfileLink(): ProfileLink = ProfileLink(
        id = id.require(),
        url = url,
        profileLinkSite = profileLinkSite?.toProfileLinkSite(),
        user = user?.toUser()
    )

    fun NetworkProfileLinkSite.toProfileLinkSite() = ProfileLinkSite(
        id = id.require(),
        name = name
    )
}