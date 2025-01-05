package io.github.drumber.kitsune.data.paging.library

import io.github.drumber.kitsune.data.common.Filter
import io.github.drumber.kitsune.data.paging.BasePagingDataSource
import io.github.drumber.kitsune.data.source.jsonapi.PageData
import io.github.drumber.kitsune.data.source.jsonapi.library.LibraryNetworkDataSource
import io.github.drumber.kitsune.data.source.jsonapi.library.model.NetworkLibraryEntry

class LibraryEntryPagingDataSource(
    private val dataSource: LibraryNetworkDataSource,
    private val filter: Filter
) : BasePagingDataSource<NetworkLibraryEntry>() {

    override suspend fun requestPage(pageOffset: Int): PageData<NetworkLibraryEntry> {
        return dataSource.getAllLibraryEntries(filter.pageOffset(pageOffset))
    }
}
