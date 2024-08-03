package io.github.drumber.kitsune.data.source.network.media

import io.github.drumber.kitsune.data.source.network.BasePagingDataSource
import io.github.drumber.kitsune.data.source.network.PageData
import io.github.drumber.kitsune.data.source.network.media.model.NetworkManga
import io.github.drumber.kitsune.data.common.Filter

class MangaPagingDataSource(
    private val dataSource: MangaNetworkDataSource,
    private val filter: Filter
) : BasePagingDataSource<NetworkManga>() {

    override suspend fun requestPage(pageOffset: Int): PageData<NetworkManga> {
        return dataSource.getAllManga(filter.pageOffset(pageOffset))
    }
}
