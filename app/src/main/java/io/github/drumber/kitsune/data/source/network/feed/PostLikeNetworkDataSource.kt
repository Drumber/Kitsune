package io.github.drumber.kitsune.data.source.network.feed

import com.github.jasminb.jsonapi.JSONAPIDocument
import io.github.drumber.kitsune.data.common.Filter
import io.github.drumber.kitsune.data.source.network.feed.api.PostLikeApi
import io.github.drumber.kitsune.data.source.network.feed.model.NetworkPostLike
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class PostLikeNetworkDataSource(
    private val postLikeApi: PostLikeApi
) {

    suspend fun getPostLikes(filter: Filter): List<NetworkPostLike> {
        return withContext(Dispatchers.IO) {
            postLikeApi.getPostLikes(filter.options).get().orEmpty()
        }
    }

    suspend fun postPostLike(postLike: NetworkPostLike): NetworkPostLike? {
        return withContext(Dispatchers.IO) {
            postLikeApi.postPostLike(JSONAPIDocument(postLike)).get()
        }
    }

    suspend fun deletePostLike(id: String) {
        withContext(Dispatchers.IO) {
            postLikeApi.deletePostLike(id)
        }
    }

}
