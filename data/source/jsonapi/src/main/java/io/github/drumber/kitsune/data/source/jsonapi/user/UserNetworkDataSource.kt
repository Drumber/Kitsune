package io.github.drumber.kitsune.data.source.jsonapi.user

import com.github.jasminb.jsonapi.JSONAPIDocument
import io.github.drumber.kitsune.data.model.Filter
import io.github.drumber.kitsune.data.source.jsonapi.user.api.UserApi
import io.github.drumber.kitsune.data.source.jsonapi.user.api.UserImageUploadApi
import io.github.drumber.kitsune.data.source.jsonapi.user.model.NetworkUser
import io.github.drumber.kitsune.data.source.jsonapi.user.model.NetworkUserImageUpload
import io.github.drumber.kitsune.data.source.jsonapi.user.model.profilelinks.NetworkProfileLink
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class UserNetworkDataSource(
    private val userApi: UserApi,
    private val imageUploadApi: UserImageUploadApi
) {

    suspend fun getSelf(baseFilter: Filter): NetworkUser? {
        val filter = baseFilter.copy()
            .filter("self", "true")

        return withContext(Dispatchers.IO) {
            userApi.getAllUsers(filter.options).get()?.firstOrNull()
        }
    }

    suspend fun getUser(userId: String, filter: Filter): NetworkUser? {
        return withContext(Dispatchers.IO) {
            userApi.getUser(userId, filter.options).get()
        }
    }

    suspend fun updateUser(userId: String, user: NetworkUser): NetworkUser? {
        return withContext(Dispatchers.IO) {
            userApi.updateUser(userId, JSONAPIDocument(user)).get()
        }
    }

    suspend fun updateUserImage(userId: String, user: NetworkUserImageUpload): Boolean {
        return withContext(Dispatchers.IO) {
            imageUploadApi.updateUserImage(userId, JSONAPIDocument(user)).isSuccessful
        }
    }

    suspend fun getProfileLinksForUser(userId: String, filter: Filter): List<NetworkProfileLink>? {
        return withContext(Dispatchers.IO) {
            userApi.getProfileLinksForUser(userId, filter.options).get()
        }
    }

    suspend fun deleteWaifuRelationship(userId: String): Boolean {
        return withContext(Dispatchers.IO) {
            userApi.deleteWaifuRelationship(userId).isSuccessful
        }
    }
}