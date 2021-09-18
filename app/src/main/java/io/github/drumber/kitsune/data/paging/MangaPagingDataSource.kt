package io.github.drumber.kitsune.data.paging

import com.github.jasminb.jsonapi.JSONAPIDocument
import io.github.drumber.kitsune.data.model.resource.Manga
import io.github.drumber.kitsune.data.service.Filter
import io.github.drumber.kitsune.data.service.manga.MangaService

class MangaPagingDataSource(
    private val service: MangaService,
    filter: Filter,
    requestType: RequestType = RequestType.ALL
) : ResourcePagingDataSource<Manga>(filter, requestType) {

    override suspend fun requestResource(filter: Filter, requestType: RequestType): JSONAPIDocument<List<Manga>> {
        return when (requestType) {
            RequestType.ALL -> service.allManga(filter.options)
            RequestType.TRENDING -> service.trending(filter.options)
        }
    }

}