package io.github.drumber.kitsune.data.paging.media

import io.github.drumber.kitsune.data.common.Filter
import io.github.drumber.kitsune.data.source.jsonapi.PageData
import io.github.drumber.kitsune.data.source.jsonapi.media.model.unit.NetworkEpisode
import io.github.drumber.kitsune.data.paging.BasePagingDataSource
import io.github.drumber.kitsune.data.source.jsonapi.media.EpisodeNetworkDataSource

class EpisodePagingDataSource(
    private val dataSource: EpisodeNetworkDataSource,
    private val filter: Filter
) : BasePagingDataSource<NetworkEpisode>() {

    override suspend fun requestPage(pageOffset: Int): PageData<NetworkEpisode> {
        return dataSource.getAllEpisodes(filter.pageOffset(pageOffset))
    }
}