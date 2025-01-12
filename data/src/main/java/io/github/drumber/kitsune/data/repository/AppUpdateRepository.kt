package io.github.drumber.kitsune.data.repository

import io.github.drumber.kitsune.data.mapper.AppUpdateMapper.toAppRelease
import io.github.drumber.kitsune.data.model.appupdate.UpdateCheckResult
import io.github.drumber.kitsune.data.source.jsonapi.appupdate.AppReleaseNetworkDataSource
import io.github.drumber.kitsune.shared.logE

class AppUpdateRepository(
    private val appReleaseDataSource: AppReleaseNetworkDataSource
) {

    suspend fun checkForUpdates(currentVersion: String): UpdateCheckResult {
        return try {
            val release = appReleaseDataSource.getLatestRelease().toAppRelease()
            if (isDifferentVersion(currentVersion, release.version)) {
                UpdateCheckResult.NewVersion(release)
            } else {
                UpdateCheckResult.NoNewVersion
            }
        } catch (e: Exception) {
            logE("Failed to fetch latest app release.", e)
            UpdateCheckResult.Error(e)
        }
    }

    private fun isDifferentVersion(currentVersion: String, newVersionTag: String): Boolean {
        // replace everything except digits and dots
        val regex = "[^\\d.]".toRegex()
        val newRawVersion = newVersionTag.replace(regex, "")
        val currentRawVersion = currentVersion.replace(regex, "")

        return newRawVersion != currentRawVersion
    }
}