package io.github.drumber.kitsune.data.source.network.media

import io.github.drumber.kitsune.data.source.network.PageData
import io.github.drumber.kitsune.data.source.network.media.api.EpisodeApi
import io.github.drumber.kitsune.data.source.network.media.model.unit.NetworkEpisode
import io.github.drumber.kitsune.data.source.network.toPageData
import io.github.drumber.kitsune.domain_old.service.Filter

class EpisodeNetworkDataSource(
    private val episodeApi: EpisodeApi
) {

    suspend fun getAllEpisodes(filter: Filter): PageData<NetworkEpisode> {
        return episodeApi.getAllEpisodes(filter.options).toPageData()
    }
}