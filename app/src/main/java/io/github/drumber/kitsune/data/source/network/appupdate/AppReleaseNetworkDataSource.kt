package io.github.drumber.kitsune.data.source.network.appupdate

import io.github.drumber.kitsune.data.source.network.appupdate.api.GitHubApi
import io.github.drumber.kitsune.data.source.network.appupdate.model.NetworkGitHubRelease
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AppReleaseNetworkDataSource(
    private val service: GitHubApi
) {

    suspend fun getLatestRelease(): NetworkGitHubRelease {
        return withContext(Dispatchers.IO) {
            service.getLatestRelease()
        }
    }
}