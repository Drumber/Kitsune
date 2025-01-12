package io.github.drumber.kitsune.data.paging.media

import io.github.drumber.kitsune.data.model.Filter
import io.github.drumber.kitsune.data.source.jsonapi.PageData
import io.github.drumber.kitsune.data.source.jsonapi.media.model.NetworkAnime
import io.github.drumber.kitsune.data.paging.BasePagingDataSource
import io.github.drumber.kitsune.data.source.jsonapi.media.AnimeNetworkDataSource

class TrendingAnimePagingDataSource(
    private val dataSource: AnimeNetworkDataSource,
    private val filter: Filter
) : BasePagingDataSource<NetworkAnime>() {

    override suspend fun requestPage(pageOffset: Int): PageData<NetworkAnime> {
        return dataSource.getTrending(filter.pageOffset(pageOffset))
    }
}
