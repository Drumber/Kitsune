package io.github.drumber.kitsune.data.source.network.user

import com.github.jasminb.jsonapi.JSONAPIDocument
import io.github.drumber.kitsune.data.common.Filter
import io.github.drumber.kitsune.data.source.network.PageData
import io.github.drumber.kitsune.data.source.network.toPageData
import io.github.drumber.kitsune.data.source.network.user.api.FollowApi
import io.github.drumber.kitsune.data.source.network.user.model.NetworkFollow
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class FollowNetworkDataSource(
    private val followApi: FollowApi
) {

    suspend fun getFollows(filter: Filter): List<NetworkFollow>? {
        return withContext(Dispatchers.IO) {
            followApi.getFollows(filter.options).get()
        }
    }

    suspend fun getFollowsPage(filter: Filter): PageData<NetworkFollow> {
        return withContext(Dispatchers.IO) {
            followApi.getFollows(filter.options).toPageData()
        }
    }

    suspend fun createFollow(follow: NetworkFollow): NetworkFollow? {
        return withContext(Dispatchers.IO) {
            followApi.createFollow(JSONAPIDocument(follow)).get()
        }
    }

    suspend fun deleteFollow(id: String): Boolean {
        return withContext(Dispatchers.IO) {
            followApi.deleteFollow(id).isSuccessful
        }
    }
}
