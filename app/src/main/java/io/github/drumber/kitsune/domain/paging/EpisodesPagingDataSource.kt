package io.github.drumber.kitsune.domain.paging

import com.github.jasminb.jsonapi.JSONAPIDocument
import io.github.drumber.kitsune.domain.model.infrastructure.media.unit.Episode
import io.github.drumber.kitsune.domain.service.Filter
import io.github.drumber.kitsune.domain.service.anime.EpisodesService

class EpisodesPagingDataSource(
    private val service: EpisodesService,
    filter: Filter
) : BasePagingDataSource<Episode>(filter) {

    override suspend fun requestService(filter: Filter): JSONAPIDocument<List<Episode>> {
        return service.allEpisodes(filter.options)
    }

}