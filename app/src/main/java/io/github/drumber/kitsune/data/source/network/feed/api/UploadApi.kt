package io.github.drumber.kitsune.data.source.network.feed.api

import com.github.jasminb.jsonapi.JSONAPIDocument
import io.github.drumber.kitsune.data.source.network.feed.model.NetworkUploadRequest
import retrofit2.http.Body
import retrofit2.http.POST

interface UploadApi {

    @POST("uploads")
    suspend fun postUpload(
        @Body upload: JSONAPIDocument<NetworkUploadRequest>
    ): JSONAPIDocument<NetworkUploadRequest>

}
