package io.github.drumber.kitsune.data.source.jsonapi.media

import io.github.drumber.kitsune.data.common.Filter
import io.github.drumber.kitsune.data.source.jsonapi.PageData
import io.github.drumber.kitsune.data.source.jsonapi.media.api.EpisodeApi
import io.github.drumber.kitsune.data.source.jsonapi.media.model.unit.NetworkEpisode
import io.github.drumber.kitsune.data.source.jsonapi.toPageData

class EpisodeNetworkDataSource(
    private val episodeApi: EpisodeApi
) {

    suspend fun getAllEpisodes(filter: Filter): PageData<NetworkEpisode> {
        return episodeApi.getAllEpisodes(filter.options).toPageData()
    }
}