package io.github.drumber.kitsune.domain.service.github

import io.github.drumber.kitsune.domain.model.infrastructure.github.GitHubRelease
import retrofit2.http.GET

interface GitHubApiService {

    @GET("releases/latest")
    suspend fun getLatestRelease(): GitHubRelease

}