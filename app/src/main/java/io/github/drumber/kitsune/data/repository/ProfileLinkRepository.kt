package io.github.drumber.kitsune.data.repository

import io.github.drumber.kitsune.data.mapper.ProfileLinksMapper.toProfileLink
import io.github.drumber.kitsune.data.mapper.ProfileLinksMapper.toProfileLinkSite
import io.github.drumber.kitsune.data.presentation.model.user.profilelinks.ProfileLink
import io.github.drumber.kitsune.data.presentation.model.user.profilelinks.ProfileLinkSite
import io.github.drumber.kitsune.data.source.network.user.ProfileLinkNetworkDataSource
import io.github.drumber.kitsune.data.source.network.user.model.profilelinks.NetworkProfileLink
import io.github.drumber.kitsune.data.common.Filter

class ProfileLinkRepository(
    private val remoteProfileLinkDataSource: ProfileLinkNetworkDataSource
) {

    suspend fun getAllProfileLinkSites(filter: Filter): List<ProfileLinkSite>? {
        return remoteProfileLinkDataSource.getAllProfileLinkSites(filter)?.map { it.toProfileLinkSite() }
    }

    suspend fun createProfileLink(profileLink: NetworkProfileLink): ProfileLink? {
        return remoteProfileLinkDataSource.createProfileLink(profileLink)?.toProfileLink()
    }

    suspend fun updateProfileLink(id: String, profileLink: NetworkProfileLink): ProfileLink? {
        return remoteProfileLinkDataSource.updateProfileLink(id, profileLink)?.toProfileLink()
    }

    suspend fun deleteProfileLink(id: String): Boolean {
        return remoteProfileLinkDataSource.deleteProfileLink(id)
    }
}