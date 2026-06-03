package io.github.drumber.kitsune.data.source.network.user

import io.github.drumber.kitsune.data.common.Filter
import io.github.drumber.kitsune.data.source.network.BasePagingDataSource
import io.github.drumber.kitsune.data.source.network.PageData
import io.github.drumber.kitsune.data.source.network.user.model.NetworkFollow

class FollowPagingDataSource(
    private val dataSource: FollowNetworkDataSource,
    private val filter: Filter
) : BasePagingDataSource<NetworkFollow>() {

    override suspend fun requestPage(pageOffset: Int): PageData<NetworkFollow> {
        return dataSource.getFollowsPage(filter.pageOffset(pageOffset))
    }
}
