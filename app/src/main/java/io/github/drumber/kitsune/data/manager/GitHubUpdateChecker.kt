package io.github.drumber.kitsune.data.manager

import io.github.drumber.kitsune.BuildConfig
import io.github.drumber.kitsune.data.model.github.GitHubRelease
import io.github.drumber.kitsune.data.service.github.GitHubApiService
import io.github.drumber.kitsune.util.logE
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class GitHubUpdateChecker(private val service: GitHubApiService) {

    suspend fun checkForUpdates(): UpdateCheckerResult = withContext(Dispatchers.IO) {
        try {
            val release = service.getLatestRelease()
            if (isDifferentVersion(release.version)) {
                UpdateCheckerResult.NewVersion(release)
            } else {
                UpdateCheckerResult.NoNewVersion
            }
        } catch (e: Exception) {
            logE("Failed to fetch latest github release.", e)
            UpdateCheckerResult.Failed(e)
        }
    }

    private fun isDifferentVersion(versionTag: String): Boolean {
        // replace everything except digits and dots
        val regex = "[^\\d.]".toRegex()
        val rawVersion = versionTag.replace(regex, "")
        val currentRawVersion = BuildConfig.VERSION_NAME.replace(regex, "")

        return rawVersion != currentRawVersion
    }

    sealed class UpdateCheckerResult {
        object NoNewVersion : UpdateCheckerResult()
        data class NewVersion(val release: GitHubRelease) : UpdateCheckerResult()
        data class Failed(val exception: Exception) : UpdateCheckerResult()
    }

}