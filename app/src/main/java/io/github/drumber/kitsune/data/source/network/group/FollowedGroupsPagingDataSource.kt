package io.github.drumber.kitsune.data.source.network.group

import io.github.drumber.kitsune.data.common.Filter
import io.github.drumber.kitsune.data.source.network.BasePagingDataSource
import io.github.drumber.kitsune.data.source.network.PageData
import io.github.drumber.kitsune.data.source.network.group.model.NetworkGroupMember

class FollowedGroupsPagingDataSource(
    private val dataSource: GroupsNetworkDataSource,
    private val filter: Filter
) : BasePagingDataSource<NetworkGroupMember>() {

    override suspend fun requestPage(pageOffset: Int): PageData<NetworkGroupMember> {
        return dataSource.getGroupMembers(filter.pageOffset(pageOffset))
    }
}
