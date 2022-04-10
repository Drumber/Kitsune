package io.github.drumber.kitsune.data.service.github

import io.github.drumber.kitsune.data.model.github.GitHubRelease
import retrofit2.http.GET

interface GitHubApiService {

    @GET("releases/latest")
    suspend fun getLatestRelease(): GitHubRelease

}