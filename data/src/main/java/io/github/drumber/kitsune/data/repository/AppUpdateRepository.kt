package io.github.drumber.kitsune.data.repository

import io.github.drumber.kitsune.core.utils.logE
import io.github.drumber.kitsune.data.mapper.AppUpdateMapper.toAppRelease
import io.github.drumber.kitsune.data.model.appupdate.UpdateCheckResult
import io.github.drumber.kitsune.data.source.jsonapi.appupdate.AppReleaseNetworkDataSource

class AppUpdateRepository(
    private val appReleaseDataSource: AppReleaseNetworkDataSource
) {

    suspend fun checkForUpdates(currentVersion: String): UpdateCheckResult {
        return try {
            val release = appReleaseDataSource.getLatestRelease().toAppRelease()
            if (isNewVersion(currentVersion, release.version)) {
                UpdateCheckResult.NewVersion(release)
            } else {
                UpdateCheckResult.NoNewVersion
            }
        } catch (e: Exception) {
            logE("Failed to fetch latest app release.", e)
            UpdateCheckResult.Error(e)
        }
    }

    private fun isNewVersion(localVersion: String, remoteVersion: String): Boolean {
        if (localVersion.trim() == remoteVersion.trim()) {
            return false
        }
        return isSemanticVersionHigher(localVersion, remoteVersion)
    }

    private fun isSemanticVersionHigher(localVersion: String, remoteVersion: String): Boolean {
        // replace everything that is not a digit at the beginning of the string
        val regex = "^\\D+".toRegex()
        // split the version strings into semantic version parts
        val localParts = localVersion.replace(regex, "").trim().split(".")
        val remoteParts = remoteVersion.replace(regex, "").trim().split(".")

        for (i in 0 until maxOf(localParts.size, remoteParts.size)) {
            val localPartString = localParts.getOrNull(i) ?: "0"
            val remotePartString = remoteParts.getOrNull(i) ?: "0"

            val localPartNumber = localPartString.toIntOrNull()
            val remotePartNumber = remotePartString.toIntOrNull()

            // Both parts are numbers: standard numeric comparison
            if (remotePartNumber != null && localPartNumber != null) {
                if (remotePartNumber != localPartNumber) {
                    return remotePartNumber > localPartNumber
                }
                // both parts are equal, continue to next part
                continue
            }

            // Remote is a number and local is a string starting with same number:
            // => remote is stable version and has higher precedence (e.g. "0" vs "0-beta1")
            if (remotePartNumber != null && localPartString.startsWith(remotePartString)) {
                if (localPartString.endsWith("-debug")) {
                    // do not report debug versions as new versions
                    return false
                }
                return true
            }

            // Local is a number and remote is a string starting with same number:
            // => local is stable version and has higher precedence
            if (localPartNumber != null && remotePartString.startsWith(localPartString)) {
                return false
            }

            // Both parts are non-numeric: fallback to string comparison (e.g. "0-beta1" vs "0-beta2")
            if (remotePartString != localPartString) {
                return remotePartString > localPartString
            }
        }
        return false
    }
}