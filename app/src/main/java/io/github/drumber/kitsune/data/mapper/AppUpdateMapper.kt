package io.github.drumber.kitsune.data.mapper

import io.github.drumber.kitsune.data.model.appupdate.AppRelease
import io.github.drumber.kitsune.data.source.jsonapi.appupdate.model.NetworkGitHubRelease

object AppUpdateMapper {
    fun NetworkGitHubRelease.toAppRelease() = AppRelease(
        version = version,
        url = url
    )
}