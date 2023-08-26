package io.github.drumber.kitsune.domain.paging

import com.github.jasminb.jsonapi.JSONAPIDocument
import io.github.drumber.kitsune.domain.model.infrastructure.media.unit.Chapter
import io.github.drumber.kitsune.domain.service.Filter
import io.github.drumber.kitsune.domain.service.manga.ChaptersService

class ChaptersPagingDataSource(
    private val service: ChaptersService,
    filter: Filter
) : BasePagingDataSource<Chapter>(filter) {

    override suspend fun requestService(filter: Filter): JSONAPIDocument<List<Chapter>> {
        return service.allChapters(filter.options)
    }

}