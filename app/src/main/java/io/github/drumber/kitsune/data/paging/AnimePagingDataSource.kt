package io.github.drumber.kitsune.data.paging

import com.github.jasminb.jsonapi.JSONAPIDocument
import io.github.drumber.kitsune.data.model.resource.Anime
import io.github.drumber.kitsune.data.service.Filter
import io.github.drumber.kitsune.data.service.anime.AnimeService

class AnimePagingDataSource(
    private val service: AnimeService,
    filter: Filter
) : ResourcePagingDataSource<Anime>(filter) {

    override suspend fun requestResource(filter: Filter): JSONAPIDocument<List<Anime>> {
        return service.allAnime(filter.options)
    }

}