package io.github.drumber.kitsune.data.source.jsonapi.media

import io.github.drumber.kitsune.data.common.Filter
import io.github.drumber.kitsune.data.source.jsonapi.PageData
import io.github.drumber.kitsune.data.source.jsonapi.media.model.production.NetworkCasting
import io.github.drumber.kitsune.data.source.jsonapi.BasePagingDataSource

class CastingPagingDataSource(
    private val dataSource: CastingNetworkDataSource,
    private val filter: Filter
) : BasePagingDataSource<NetworkCasting>() {

    override suspend fun requestPage(pageOffset: Int): PageData<NetworkCasting> {
        return dataSource.getAllCastings(filter.pageOffset(pageOffset))
    }
}