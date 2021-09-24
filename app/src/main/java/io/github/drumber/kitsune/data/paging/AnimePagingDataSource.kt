package io.github.drumber.kitsune.data.paging

import com.github.jasminb.jsonapi.JSONAPIDocument
import io.github.drumber.kitsune.data.model.resource.Anime
import io.github.drumber.kitsune.data.model.toPage
import io.github.drumber.kitsune.data.service.Filter
import io.github.drumber.kitsune.data.service.anime.AnimeService

class AnimePagingDataSource(
    private val service: AnimeService,
    filter: Filter,
    requestType: RequestType = RequestType.ALL
) : ResourcePagingDataSource<Anime>(filter, requestType) {

    override suspend fun requestResource(filter: Filter, requestType: RequestType, params: LoadParams<Int>): Response {
        val response: JSONAPIDocument<List<Anime>> = when (requestType) {
            RequestType.ALL -> service.allAnime(filter.options)
            RequestType.TRENDING -> service.trending(filter.options)
        }
        return Response(
            data = response.get(),
            page = response.links?.toPage()
        )
    }

}