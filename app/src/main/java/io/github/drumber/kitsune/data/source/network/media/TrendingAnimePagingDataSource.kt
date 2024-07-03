package io.github.drumber.kitsune.data.source.network.media

import io.github.drumber.kitsune.data.source.network.BasePagingDataSource
import io.github.drumber.kitsune.data.source.network.PageData
import io.github.drumber.kitsune.data.source.network.media.model.NetworkAnime
import io.github.drumber.kitsune.domain_old.service.Filter

class TrendingAnimePagingDataSource(
    private val dataSource: AnimeNetworkDataSource,
    private val filter: Filter
) : BasePagingDataSource<NetworkAnime>() {

    override suspend fun requestPage(pageOffset: Int): PageData<NetworkAnime> {
        return dataSource.getTrending(filter.pageOffset(pageOffset))
    }
}
