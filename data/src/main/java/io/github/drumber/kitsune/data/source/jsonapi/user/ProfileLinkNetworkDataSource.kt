package io.github.drumber.kitsune.data.source.jsonapi.user

import io.github.drumber.kitsune.data.model.Filter
import io.github.drumber.kitsune.data.source.jsonapi.user.api.ProfileLinkApi
import io.github.drumber.kitsune.data.source.jsonapi.user.model.profilelinks.NetworkProfileLink
import io.github.drumber.kitsune.data.source.jsonapi.user.model.profilelinks.NetworkProfileLinkSite
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ProfileLinkNetworkDataSource(
    private val profileLinkApi: ProfileLinkApi
) {

    suspend fun getAllProfileLinkSites(filter: Filter): List<NetworkProfileLinkSite>? {
        return withContext(Dispatchers.IO) {
            profileLinkApi.getAllProfileLinkSites(filter.options).get()
        }
    }

    suspend fun createProfileLink(profileLink: NetworkProfileLink): NetworkProfileLink? {
        return withContext(Dispatchers.IO) {
            profileLinkApi.createProfileLink(profileLink).get()
        }
    }

    suspend fun updateProfileLink(id: String, profileLink: NetworkProfileLink): NetworkProfileLink? {
        return withContext(Dispatchers.IO) {
            profileLinkApi.updateProfileLink(id, profileLink).get()
        }
    }

    suspend fun deleteProfileLink(id: String): Boolean {
        return withContext(Dispatchers.IO) {
            profileLinkApi.deleteProfileLink(id).isSuccessful
        }
    }
}