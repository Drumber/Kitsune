package io.github.drumber.kitsune.data.source.network.media

import io.github.drumber.kitsune.data.source.network.BasePagingDataSource
import io.github.drumber.kitsune.data.source.network.PageData
import io.github.drumber.kitsune.data.source.network.media.model.NetworkAnime
import io.github.drumber.kitsune.data.common.Filter

class AnimePagingDataSource(
    private val dataSource: AnimeNetworkDataSource,
    private val filter: Filter
) : BasePagingDataSource<NetworkAnime>() {

    override suspend fun requestPage(pageOffset: Int): PageData<NetworkAnime> {
        return dataSource.getAllAnime(filter.pageOffset(pageOffset))
    }
}
