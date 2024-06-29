package io.github.drumber.kitsune.domain_old.service.user

import com.github.jasminb.jsonapi.JSONAPIDocument
import io.github.drumber.kitsune.domain_old.model.infrastructure.user.UserImageUpload
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.PATCH
import retrofit2.http.Path

interface UserImageUploadService {

    @PATCH("users/{id}")
    suspend fun updateUserImage(
        @Path("id") id: String,
        @Body user: JSONAPIDocument<UserImageUpload>
    ): Response<Unit>

}