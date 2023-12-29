package io.github.drumber.kitsune.domain.service.user

import com.github.jasminb.jsonapi.JSONAPIDocument
import io.github.drumber.kitsune.domain.model.infrastructure.user.UserImageUpload
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.*

interface UserImageUploadService {

    @PATCH("users/{id}")
    fun updateUserImage(
        @Path("id") id: String,
        @Body user: JSONAPIDocument<UserImageUpload>
    ): Call<ResponseBody>

}