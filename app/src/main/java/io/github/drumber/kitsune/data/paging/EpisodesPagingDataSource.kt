package io.github.drumber.kitsune.data.paging

import com.github.jasminb.jsonapi.JSONAPIDocument
import io.github.drumber.kitsune.data.model.unit.Episode
import io.github.drumber.kitsune.data.service.Filter
import io.github.drumber.kitsune.data.service.anime.EpisodesService

class EpisodesPagingDataSource(
    private val service: EpisodesService,
    filter: Filter
) : BasePagingDataSource<Episode>(filter) {

    override suspend fun requestService(filter: Filter): JSONAPIDocument<List<Episode>> {
        return service.allEpisodes(filter.options)
    }

}