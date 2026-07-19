package io.github.drumber.kitsune.data.source.network.group

import io.github.drumber.kitsune.data.common.Filter
import io.github.drumber.kitsune.data.source.network.BasePagingDataSource
import io.github.drumber.kitsune.data.source.network.PageData
import io.github.drumber.kitsune.data.source.network.group.model.NetworkGroup

class GroupsPagingDataSource(
    private val dataSource: GroupsNetworkDataSource,
    private val filter: Filter
) : BasePagingDataSource<NetworkGroup>() {

    override suspend fun requestPage(pageOffset: Int): PageData<NetworkGroup> {
        return dataSource.getGroups(filter.pageOffset(pageOffset))
    }
}
