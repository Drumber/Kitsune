package io.github.drumber.kitsune.domain_old.paging

import com.github.jasminb.jsonapi.JSONAPIDocument
import io.github.drumber.kitsune.domain_old.model.infrastructure.media.unit.Episode
import io.github.drumber.kitsune.domain_old.service.Filter
import io.github.drumber.kitsune.domain_old.service.anime.EpisodesService

class EpisodesPagingDataSource(
    private val service: EpisodesService,
    filter: Filter
) : BasePagingDataSource<Episode>(filter) {

    override suspend fun requestService(filter: Filter): JSONAPIDocument<List<Episode>> {
        return service.allEpisodes(filter.options)
    }

}