package io.github.drumber.kitsune.data.source.network.feed

import com.github.jasminb.jsonapi.JSONAPIDocument
import io.github.drumber.kitsune.data.source.network.feed.api.PostApi
import io.github.drumber.kitsune.data.source.network.feed.model.NetworkPost
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class PostNetworkDataSource(
    private val postApi: PostApi
) {

    suspend fun getPost(id: String, filter: Map<String, String> = emptyMap()): NetworkPost? {
        return withContext(Dispatchers.IO) {
            postApi.getPost(id, filter).get()
        }
    }

    suspend fun postPost(post: NetworkPost): NetworkPost? {
        return withContext(Dispatchers.IO) {
            postApi.postPost(JSONAPIDocument(post)).get()
        }
    }

    suspend fun updatePost(id: String, post: NetworkPost): NetworkPost? {
        return withContext(Dispatchers.IO) {
            postApi.updatePost(id, JSONAPIDocument(post)).get()
        }
    }

    suspend fun deletePost(id: String) {
        withContext(Dispatchers.IO) {
            postApi.deletePost(id)
        }
    }

}
