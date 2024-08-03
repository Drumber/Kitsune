package io.github.drumber.kitsune.data.source.network.auth.api

import io.github.drumber.kitsune.data.source.network.auth.model.NetworkAccessToken
import io.github.drumber.kitsune.data.source.network.auth.model.ObtainAccessToken
import io.github.drumber.kitsune.data.source.network.auth.model.RefreshAccessToken
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthenticationApi {

    @POST("token")
    suspend fun obtainAccessToken(
        @Body obtainAccessToken: ObtainAccessToken
    ): NetworkAccessToken

    @POST("token")
    suspend fun refreshToken(
        @Body refreshAccessToken: RefreshAccessToken
    ): NetworkAccessToken

}