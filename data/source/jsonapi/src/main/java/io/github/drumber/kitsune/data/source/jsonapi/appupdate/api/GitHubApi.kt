package io.github.drumber.kitsune.data.source.jsonapi.appupdate.api

import io.github.drumber.kitsune.data.source.jsonapi.appupdate.model.NetworkGitHubRelease
import retrofit2.http.GET

interface GitHubApi {

    @GET("releases/latest")
    suspend fun getLatestRelease(): NetworkGitHubRelease

}