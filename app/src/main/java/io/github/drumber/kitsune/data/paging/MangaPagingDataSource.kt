package io.github.drumber.kitsune.data.paging

import com.github.jasminb.jsonapi.JSONAPIDocument
import io.github.drumber.kitsune.data.model.resource.manga.Manga
import io.github.drumber.kitsune.data.service.Filter
import io.github.drumber.kitsune.data.service.manga.MangaService

class MangaPagingDataSource(
    private val service: MangaService,
    filter: Filter
) : ResourcePagingDataSource<Manga>(filter) {

    override suspend fun requestResource(filter: Filter): JSONAPIDocument<List<Manga>> {
        return service.allManga(filter.options)
    }

}