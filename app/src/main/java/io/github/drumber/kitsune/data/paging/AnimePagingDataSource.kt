package io.github.drumber.kitsune.data.paging

import com.github.jasminb.jsonapi.JSONAPIDocument
import io.github.drumber.kitsune.data.model.media.Anime
import io.github.drumber.kitsune.data.service.Filter
import io.github.drumber.kitsune.data.service.anime.AnimeService

class AnimePagingDataSource(
    private val service: AnimeService,
    filter: Filter,
    requestType: RequestType = RequestType.ALL
) : MediaPagingDataSource<Anime>(filter, requestType) {

    override suspend fun requestMedia(filter: Filter, requestType: RequestType): JSONAPIDocument<List<Anime>> {
        return when (requestType) {
            RequestType.ALL -> service.allAnime(filter.options)
            RequestType.TRENDING -> service.trending(filter.options)
        }
    }

}
