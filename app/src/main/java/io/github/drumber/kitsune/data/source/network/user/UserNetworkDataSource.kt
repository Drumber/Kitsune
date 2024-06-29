package io.github.drumber.kitsune.data.source.network.user

import io.github.drumber.kitsune.data.source.network.user.api.UserApi
import io.github.drumber.kitsune.data.source.network.user.model.NetworkUser
import io.github.drumber.kitsune.domain_old.service.Filter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class UserNetworkDataSource(
    private val userApi: UserApi
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

}