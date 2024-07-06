package io.github.drumber.kitsune.data.source.network.media

import io.github.drumber.kitsune.data.source.network.BasePagingDataSource
import io.github.drumber.kitsune.data.source.network.PageData
import io.github.drumber.kitsune.data.source.network.media.model.production.NetworkCasting
import io.github.drumber.kitsune.data.common.Filter

class CastingPagingDataSource(
    private val dataSource: CastingNetworkDataSource,
    private val filter: Filter
) : BasePagingDataSource<NetworkCasting>() {

    override suspend fun requestPage(pageOffset: Int): PageData<NetworkCasting> {
        return dataSource.getAllCastings(filter.pageOffset(pageOffset))
    }
}