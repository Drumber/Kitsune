package io.github.drumber.kitsune.data.repository

import io.github.drumber.kitsune.data.common.Filter
import io.github.drumber.kitsune.data.mapper.ProfileLinksMapper.toProfileLink
import io.github.drumber.kitsune.data.mapper.ProfileLinksMapper.toProfileLinkSite
import io.github.drumber.kitsune.data.presentation.model.user.profilelinks.ProfileLink
import io.github.drumber.kitsune.data.presentation.model.user.profilelinks.ProfileLinkSite
import io.github.drumber.kitsune.data.source.jsonapi.user.ProfileLinkNetworkDataSource
import io.github.drumber.kitsune.data.source.jsonapi.user.model.NetworkUser
import io.github.drumber.kitsune.data.source.jsonapi.user.model.profilelinks.NetworkProfileLink
import io.github.drumber.kitsune.data.source.jsonapi.user.model.profilelinks.NetworkProfileLinkSite

class ProfileLinkRepository(
    private val remoteProfileLinkDataSource: ProfileLinkNetworkDataSource
) {

    suspend fun getAllProfileLinkSites(filter: Filter): List<ProfileLinkSite>? {
        return remoteProfileLinkDataSource.getAllProfileLinkSites(filter)
            ?.map { it.toProfileLinkSite() }
    }

    suspend fun createProfileLink(userId: String, siteId: String, url: String): ProfileLink? {
        val newProfileLink = NetworkProfileLink.empty().copy(
            url = url,
            profileLinkSite = NetworkProfileLinkSite(
                id = siteId,
                name = null
            ),
            user = NetworkUser(id = userId)
        )
        return remoteProfileLinkDataSource.createProfileLink(newProfileLink)?.toProfileLink()
    }

    suspend fun updateProfileLink(
        userId: String,
        profileLinkId: String,
        url: String
    ): ProfileLink? {
        val updatedProfileLink = NetworkProfileLink.empty().copy(
            id = profileLinkId,
            url = url,
            user = NetworkUser(id = userId)
        )
        return remoteProfileLinkDataSource.updateProfileLink(profileLinkId, updatedProfileLink)
            ?.toProfileLink()
    }

    suspend fun deleteProfileLink(id: String): Boolean {
        return remoteProfileLinkDataSource.deleteProfileLink(id)
    }
}