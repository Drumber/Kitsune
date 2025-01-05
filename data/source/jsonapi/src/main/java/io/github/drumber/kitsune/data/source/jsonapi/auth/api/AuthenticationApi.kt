package io.github.drumber.kitsune.data.source.jsonapi.auth.api

import io.github.drumber.kitsune.data.source.jsonapi.auth.model.NetworkAccessToken
import io.github.drumber.kitsune.data.source.jsonapi.auth.model.ObtainAccessToken
import io.github.drumber.kitsune.data.source.jsonapi.auth.model.RefreshAccessToken
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