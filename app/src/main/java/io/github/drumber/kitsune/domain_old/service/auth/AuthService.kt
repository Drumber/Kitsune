package io.github.drumber.kitsune.domain_old.service.auth

import io.github.drumber.kitsune.domain_old.model.infrastructure.auth.AccessToken
import io.github.drumber.kitsune.domain_old.model.infrastructure.auth.ObtainAccessToken
import io.github.drumber.kitsune.domain_old.model.infrastructure.auth.RefreshAccessToken
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthService {

    @POST("token")
    suspend fun obtainAccessToken(
        @Body obtainAccessToken: ObtainAccessToken
    ): AccessToken

    @POST("token")
    suspend fun refreshToken(
        @Body refreshAccessToken: RefreshAccessToken
    ): AccessToken

}