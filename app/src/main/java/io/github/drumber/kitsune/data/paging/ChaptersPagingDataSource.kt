package io.github.drumber.kitsune.data.paging

import com.github.jasminb.jsonapi.JSONAPIDocument
import io.github.drumber.kitsune.data.model.resource.Chapter
import io.github.drumber.kitsune.data.service.Filter
import io.github.drumber.kitsune.data.service.manga.ChaptersService

class ChaptersPagingDataSource(
    private val service: ChaptersService,
    filter: Filter
) : BasePagingDataSource<Chapter>(filter) {

    override suspend fun requestService(filter: Filter): JSONAPIDocument<List<Chapter>> {
        return service.allChapters(filter.options)
    }

}