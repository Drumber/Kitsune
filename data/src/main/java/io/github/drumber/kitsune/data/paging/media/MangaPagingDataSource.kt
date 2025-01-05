package io.github.drumber.kitsune.data.paging.media

import io.github.drumber.kitsune.data.common.Filter
import io.github.drumber.kitsune.data.source.jsonapi.PageData
import io.github.drumber.kitsune.data.source.jsonapi.media.model.NetworkManga
import io.github.drumber.kitsune.data.paging.BasePagingDataSource
import io.github.drumber.kitsune.data.source.jsonapi.media.MangaNetworkDataSource

class MangaPagingDataSource(
    private val dataSource: MangaNetworkDataSource,
    private val filter: Filter
) : BasePagingDataSource<NetworkManga>() {

    override suspend fun requestPage(pageOffset: Int): PageData<NetworkManga> {
        return dataSource.getAllManga(filter.pageOffset(pageOffset))
    }
}
