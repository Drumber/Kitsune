package io.github.drumber.kitsune.data.source.network.group

import io.github.drumber.kitsune.data.common.Filter
import io.github.drumber.kitsune.data.source.network.PageData
import io.github.drumber.kitsune.data.source.network.group.api.GroupsApi
import io.github.drumber.kitsune.data.source.network.group.model.NetworkGroup
import io.github.drumber.kitsune.data.source.network.group.model.NetworkGroupCategory
import io.github.drumber.kitsune.data.source.network.group.model.NetworkGroupMember
import io.github.drumber.kitsune.data.source.network.toPageData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class GroupsNetworkDataSource(
    private val groupsApi: GroupsApi
) {

    suspend fun getGroups(filter: Filter): PageData<NetworkGroup> {
        return withContext(Dispatchers.IO) {
            groupsApi.getGroups(filter.options).toPageData()
        }
    }

    suspend fun getGroup(id: String, filter: Filter): NetworkGroup? {
        return withContext(Dispatchers.IO) {
            groupsApi.getGroup(id, filter.options).get()
        }
    }

    suspend fun getGroupCategories(filter: Filter): List<NetworkGroupCategory> {
        return withContext(Dispatchers.IO) {
            groupsApi.getGroupCategories(filter.options).get().orEmpty()
        }
    }

    suspend fun getGroupMembers(filter: Filter): PageData<NetworkGroupMember> {
        return withContext(Dispatchers.IO) {
            groupsApi.getGroupMembers(filter.options).toPageData()
        }
    }

}
