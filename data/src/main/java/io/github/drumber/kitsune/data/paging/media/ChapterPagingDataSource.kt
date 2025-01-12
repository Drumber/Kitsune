package io.github.drumber.kitsune.data.paging.media

import io.github.drumber.kitsune.data.model.Filter
import io.github.drumber.kitsune.data.source.jsonapi.PageData
import io.github.drumber.kitsune.data.source.jsonapi.media.model.unit.NetworkChapter
import io.github.drumber.kitsune.data.paging.BasePagingDataSource
import io.github.drumber.kitsune.data.source.jsonapi.media.ChapterNetworkDataSource

class ChapterPagingDataSource(
    private val dataSource: ChapterNetworkDataSource,
    private val filter: Filter
) : BasePagingDataSource<NetworkChapter>() {

    override suspend fun requestPage(pageOffset: Int): PageData<NetworkChapter> {
        return dataSource.getAllChapters(filter.pageOffset(pageOffset))
    }
}