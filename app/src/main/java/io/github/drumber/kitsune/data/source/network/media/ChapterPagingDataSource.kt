package io.github.drumber.kitsune.data.source.network.media

import io.github.drumber.kitsune.data.source.network.BasePagingDataSource
import io.github.drumber.kitsune.data.source.network.PageData
import io.github.drumber.kitsune.data.source.network.media.model.unit.NetworkChapter
import io.github.drumber.kitsune.data.common.Filter

class ChapterPagingDataSource(
    private val dataSource: ChapterNetworkDataSource,
    private val filter: Filter
) : BasePagingDataSource<NetworkChapter>() {

    override suspend fun requestPage(pageOffset: Int): PageData<NetworkChapter> {
        return dataSource.getAllChapters(filter.pageOffset(pageOffset))
    }
}