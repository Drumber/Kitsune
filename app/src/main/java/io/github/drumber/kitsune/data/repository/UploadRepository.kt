package io.github.drumber.kitsune.data.repository

import io.github.drumber.kitsune.data.source.network.feed.UploadNetworkDataSource
import io.github.drumber.kitsune.data.source.network.feed.model.NetworkUploadRequest
import io.github.drumber.kitsune.data.source.network.user.model.NetworkUser

class UploadRepository(
    private val uploadNetworkDataSource: UploadNetworkDataSource
) {

    /**
     * Uploads a single image and returns the created upload id, or `null` when the server response
     * is empty.
     *
     * @param userId id of the uploading user.
     * @param contentDataUri base64 data URI of the image (e.g. `data:image/jpeg;base64,...`).
     * @param order zero-based position of the image among the post's uploads.
     */
    suspend fun uploadImage(userId: String, contentDataUri: String, order: Int): String? {
        val upload = NetworkUploadRequest(
            id = null,
            content = contentDataUri,
            uploadOrder = order,
            user = NetworkUser(id = userId)
        )
        return uploadNetworkDataSource.postUpload(upload)?.id
    }

}
