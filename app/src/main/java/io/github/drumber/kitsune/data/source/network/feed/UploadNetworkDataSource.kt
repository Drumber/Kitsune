package io.github.drumber.kitsune.data.source.network.feed

import com.github.jasminb.jsonapi.JSONAPIDocument
import io.github.drumber.kitsune.data.source.network.feed.api.UploadApi
import io.github.drumber.kitsune.data.source.network.feed.model.NetworkUploadRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class UploadNetworkDataSource(
    private val uploadApi: UploadApi
) {

    suspend fun postUpload(upload: NetworkUploadRequest): NetworkUploadRequest? {
        return withContext(Dispatchers.IO) {
            uploadApi.postUpload(JSONAPIDocument(upload)).get()
        }
    }

}
