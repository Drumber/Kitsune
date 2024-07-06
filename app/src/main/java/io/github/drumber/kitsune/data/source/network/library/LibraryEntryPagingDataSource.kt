package io.github.drumber.kitsune.data.source.network.library

import io.github.drumber.kitsune.data.source.network.BasePagingDataSource
import io.github.drumber.kitsune.data.source.network.PageData
import io.github.drumber.kitsune.data.source.network.library.model.NetworkLibraryEntry
import io.github.drumber.kitsune.data.common.Filter

class LibraryEntryPagingDataSource(
    private val dataSource: LibraryNetworkDataSource,
    private val filter: Filter
) : BasePagingDataSource<NetworkLibraryEntry>() {

    override suspend fun requestPage(pageOffset: Int): PageData<NetworkLibraryEntry> {
        return dataSource.getAllLibraryEntries(filter.pageOffset(pageOffset))
    }
}
