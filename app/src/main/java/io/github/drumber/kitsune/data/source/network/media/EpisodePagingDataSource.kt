package io.github.drumber.kitsune.data.source.network.media

import io.github.drumber.kitsune.data.source.network.BasePagingDataSource
import io.github.drumber.kitsune.data.source.network.PageData
import io.github.drumber.kitsune.data.source.network.media.model.unit.NetworkEpisode
import io.github.drumber.kitsune.data.common.Filter

class EpisodePagingDataSource(
    private val dataSource: EpisodeNetworkDataSource,
    private val filter: Filter
) : BasePagingDataSource<NetworkEpisode>() {

    override suspend fun requestPage(pageOffset: Int): PageData<NetworkEpisode> {
        return dataSource.getAllEpisodes(filter.pageOffset(pageOffset))
    }
}