package io.github.drumber.kitsune.data.source.jsonapi.media

import io.github.drumber.kitsune.data.common.Filter
import io.github.drumber.kitsune.data.source.jsonapi.PageData
import io.github.drumber.kitsune.data.source.jsonapi.media.model.NetworkAnime
import io.github.drumber.kitsune.data.source.jsonapi.BasePagingDataSource

class AnimePagingDataSource(
    private val dataSource: AnimeNetworkDataSource,
    private val filter: Filter
) : BasePagingDataSource<NetworkAnime>() {

    override suspend fun requestPage(pageOffset: Int): PageData<NetworkAnime> {
        return dataSource.getAllAnime(filter.pageOffset(pageOffset))
    }
}
