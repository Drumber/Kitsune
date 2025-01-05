package io.github.drumber.kitsune.data.repository

import io.github.drumber.kitsune.BuildConfig
import io.github.drumber.kitsune.data.mapper.AppUpdateMapper.toAppRelease
import io.github.drumber.kitsune.data.presentation.model.appupdate.UpdateCheckResult
import io.github.drumber.kitsune.data.source.jsonapi.appupdate.AppReleaseNetworkDataSource
import io.github.drumber.kitsune.util.logE

class AppUpdateRepository(
    private val appReleaseDataSource: AppReleaseNetworkDataSource
) {

    suspend fun checkForUpdates(): UpdateCheckResult {
        return try {
            val release = appReleaseDataSource.getLatestRelease().toAppRelease()
            if (isDifferentVersion(release.version)) {
                UpdateCheckResult.NewVersion(release)
            } else {
                UpdateCheckResult.NoNewVersion
            }
        } catch (e: Exception) {
            logE("Failed to fetch latest app release.", e)
            UpdateCheckResult.Error(e)
        }
    }

    private fun isDifferentVersion(versionTag: String): Boolean {
        // replace everything except digits and dots
        val regex = "[^\\d.]".toRegex()
        val rawVersion = versionTag.replace(regex, "")
        val currentRawVersion = BuildConfig.VERSION_NAME.replace(regex, "")

        return rawVersion != currentRawVersion
    }
}