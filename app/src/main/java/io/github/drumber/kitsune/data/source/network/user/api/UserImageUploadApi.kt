package io.github.drumber.kitsune.data.source.network.user.api

import com.github.jasminb.jsonapi.JSONAPIDocument
import io.github.drumber.kitsune.data.source.network.user.model.NetworkUserImageUpload
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.PATCH
import retrofit2.http.Path

interface UserImageUploadApi {

    @PATCH("users/{id}")
    suspend fun updateUserImage(
        @Path("id") id: String,
        @Body user: JSONAPIDocument<NetworkUserImageUpload>
    ): Response<Unit>

}