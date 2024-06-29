package io.github.drumber.kitsune.domain_old.paging

import com.github.jasminb.jsonapi.JSONAPIDocument
import io.github.drumber.kitsune.domain_old.model.infrastructure.media.unit.Chapter
import io.github.drumber.kitsune.domain_old.service.Filter
import io.github.drumber.kitsune.domain_old.service.manga.ChaptersService

class ChaptersPagingDataSource(
    private val service: ChaptersService,
    filter: Filter
) : BasePagingDataSource<Chapter>(filter) {

    override suspend fun requestService(filter: Filter): JSONAPIDocument<List<Chapter>> {
        return service.allChapters(filter.options)
    }

}