package io.github.drumber.kitsune.data.source.jsonapi.media

import io.github.drumber.kitsune.data.common.Filter
import io.github.drumber.kitsune.data.source.jsonapi.PageData
import io.github.drumber.kitsune.data.source.jsonapi.media.model.unit.NetworkChapter
import io.github.drumber.kitsune.data.source.jsonapi.BasePagingDataSource

class ChapterPagingDataSource(
    private val dataSource: ChapterNetworkDataSource,
    private val filter: Filter
) : BasePagingDataSource<NetworkChapter>() {

    override suspend fun requestPage(pageOffset: Int): PageData<NetworkChapter> {
        return dataSource.getAllChapters(filter.pageOffset(pageOffset))
    }
}