package io.github.drumber.kitsune.data.service.auth

import io.github.drumber.kitsune.constants.Kitsu
import io.github.drumber.kitsune.data.model.auth.AccessToken
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST

interface AuthService {

    @FormUrlEncoded
    @POST("token")
    suspend fun obtainAccessToken(
        @Field("grant_type") grantType: String = "password",
        @Field("username") username: String,
        @Field("password") password: String,
        @Field("client_id") clientId: String = Kitsu.CLIENT_ID,
        @Field("client_secret") clientSecret: String = Kitsu.CLIENT_SECRET
    ): AccessToken

    @FormUrlEncoded
    @POST("token")
    suspend fun refreshToken(
        @Field("grant_type") grantType: String = "refresh_token",
        @Field("refresh_token") refreshToken: String,
        @Field("client_id") clientId: String = Kitsu.CLIENT_ID,
        @Field("client_secret") clientSecret: String = Kitsu.CLIENT_SECRET
    ): AccessToken

}