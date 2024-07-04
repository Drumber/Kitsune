package io.github.drumber.kitsune.data.repository

import androidx.paging.ExperimentalPagingApi
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingSource
import androidx.paging.map
import io.github.drumber.kitsune.constants.Repository
import io.github.drumber.kitsune.data.mapper.LibraryMapper.toLibraryEntry
import io.github.drumber.kitsune.data.mapper.LibraryMapper.toLocalLibraryStatus
import io.github.drumber.kitsune.data.presentation.model.library.LibraryEntryFilter
import io.github.drumber.kitsune.data.presentation.model.library.LibraryEntryKind
import io.github.drumber.kitsune.data.source.local.LocalDatabase
import io.github.drumber.kitsune.data.source.local.library.dao.LibraryEntryDao
import io.github.drumber.kitsune.data.source.local.library.model.LocalLibraryEntry
import io.github.drumber.kitsune.data.source.local.library.model.LocalLibraryMedia.MediaType
import io.github.drumber.kitsune.data.source.network.library.LibraryEntryPagingDataSource
import io.github.drumber.kitsune.data.source.network.library.LibraryNetworkDataSource
import io.github.drumber.kitsune.data.utils.InvalidatingPagingSourceFactory
import io.github.drumber.kitsune.domain_old.service.Filter
import kotlinx.coroutines.flow.map

class LibraryRepository(
    private val remoteLibraryDataSource: LibraryNetworkDataSource,
    private val database: LocalDatabase
) {

    private val invalidatingPagingSourceFactory =
        InvalidatingPagingSourceFactory<Int, LocalLibraryEntry, LibraryEntryFilter> {
            database.libraryEntryDao().getLibraryEntriesByFilter(it)
        }

    @OptIn(ExperimentalPagingApi::class)
    fun libraryEntries(pageSize: Int, filter: LibraryEntryFilter) = Pager(
        config = PagingConfig(
            pageSize = pageSize,
            maxSize = Repository.MAX_CACHED_ITEMS
        ),
        remoteMediator = LibraryEntryRemoteMediator(
            filter.pageSize(pageSize),
            remoteLibraryDataSource,
            database
        ),
        pagingSourceFactory = { invalidatingPagingSourceFactory.createPagingSource(filter) }
    ).flow.map { pagingData ->
        pagingData.map { it.toLibraryEntry() }
    }

    fun searchLibraryEntries(pageSize: Int, filter: Filter) = Pager(
        config = PagingConfig(
            pageSize = pageSize,
            maxSize = Repository.MAX_CACHED_ITEMS
        ),
        pagingSourceFactory = {
            LibraryEntryPagingDataSource(
                remoteLibraryDataSource,
                filter.pageLimit(pageSize)
            )
        }
    ).flow.map { pagingData ->
        pagingData.map { it.toLibraryEntry() }
    }

    fun invalidatePagingSources() {
        invalidatingPagingSourceFactory.invalidate()
    }

    private fun LibraryEntryDao.getLibraryEntriesByFilter(
        filter: LibraryEntryFilter
    ): PagingSource<Int, LocalLibraryEntry> {
        val status = filter.libraryStatus.map { it.toLocalLibraryStatus() }
        val hasStatus = status.isNotEmpty()
        val kind = filter.kind
        return when {
            kind == LibraryEntryKind.Anime && hasStatus -> allLibraryEntriesByTypeAndStatusPagingSource(
                MediaType.Anime,
                status
            )

            kind == LibraryEntryKind.Anime && !hasStatus -> allLibraryEntriesByTypePagingSource(
                MediaType.Anime
            )

            kind == LibraryEntryKind.Manga && hasStatus -> allLibraryEntriesByTypeAndStatusPagingSource(
                MediaType.Manga,
                status
            )

            kind == LibraryEntryKind.Manga && !hasStatus -> allLibraryEntriesByTypePagingSource(
                MediaType.Manga
            )

            kind == LibraryEntryKind.All && hasStatus -> allLibraryEntriesByStatusPagingSource(
                status
            )

            else -> allLibraryEntriesPagingSource()
        }
    }
}