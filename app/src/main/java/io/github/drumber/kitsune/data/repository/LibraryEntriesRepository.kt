package io.github.drumber.kitsune.data.repository

import androidx.paging.ExperimentalPagingApi
import androidx.paging.Pager
import androidx.paging.PagingConfig
import io.github.drumber.kitsune.constants.Repository
import io.github.drumber.kitsune.data.paging.LibraryEntriesRemoteMediator
import io.github.drumber.kitsune.data.room.ResourceDatabase
import io.github.drumber.kitsune.data.service.Filter
import io.github.drumber.kitsune.data.service.library.LibraryEntriesService

class LibraryEntriesRepository(private val service: LibraryEntriesService, private val db: ResourceDatabase) {

    @OptIn(ExperimentalPagingApi::class)
    fun libraryEntries(pageSize: Int, filter: Filter) = Pager(
        config = PagingConfig(
            pageSize = pageSize,
            maxSize = Repository.MAX_CACHED_ITEMS
        ),
        remoteMediator = LibraryEntriesRemoteMediator(filter.pageLimit(pageSize), service, db),
        pagingSourceFactory = { db.libraryEntryDao().getLibraryEntry() }
    ).flow

}