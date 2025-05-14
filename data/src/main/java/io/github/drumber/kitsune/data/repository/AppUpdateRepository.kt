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
        if (areVersionStringsEqual(localVersion, remoteVersion)) {
            return false
        }
        return isSemanticVersionHigher(localVersion, remoteVersion) ?: return true // if semantic version check fails, assume it's a new version
    }

    private fun areVersionStringsEqual(localVersion: String, remoteVersion: String): Boolean {
        // replace everything except digits and dots
        val regex = "[^\\d.]".toRegex()
        val newRawVersion = remoteVersion.replace(regex, "")
        val currentRawVersion = localVersion.replace(regex, "")
        return newRawVersion == currentRawVersion
    }

    private fun isSemanticVersionHigher(localVersion: String, remoteVersion: String): Boolean? {
        // replace everything that is not a digit at the beginning of the string
        val regex = "^\\D+".toRegex()
        // split the version strings into semantic version parts
        val localParts = localVersion.replace(regex, "").trim().split(".")
        val remoteParts = remoteVersion.replace(regex, "").trim().split(".")
        println("localParts: $localParts  remoteParts: $remoteParts")

        for (i in 0 until maxOf(localParts.size, remoteParts.size)) {
            val localPart = (localParts.getOrNull(i) ?: "0").toIntOrNull() ?: return null
            val remotePart = (remoteParts.getOrNull(i) ?: "0").toIntOrNull() ?: return null

            if (remotePart > localPart) {
                return true
            } else if (remotePart < localPart) {
                return false
            }
        }
        return false
    }
}