package io.github.drumber.kitsune.data.repository.library

import androidx.paging.ExperimentalPagingApi
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.map
import io.github.drumber.kitsune.constants.Repository
import io.github.drumber.kitsune.data.common.Filter
import io.github.drumber.kitsune.data.common.library.LibraryFilterOptions
import io.github.drumber.kitsune.data.mapper.LibraryMapper.toLibraryEntry
import io.github.drumber.kitsune.data.mapper.LibraryMapper.toLibraryEntryWithModificationAndNextUnit
import io.github.drumber.kitsune.data.presentation.model.library.LibraryEntryFilter
import io.github.drumber.kitsune.data.source.graphql.library.LibraryApolloDataSource
import io.github.drumber.kitsune.data.source.jsonapi.library.LibraryNetworkDataSource
import io.github.drumber.kitsune.data.source.local.library.LibraryLocalDataSource
import io.github.drumber.kitsune.data.source.local.library.model.LocalLibraryEntry
import io.github.drumber.kitsune.data.source.local.library.model.LocalLibraryEntryWithModificationAndNextMediaUnit
import io.github.drumber.kitsune.data.source.local.mapper.toLocalLibraryStatus
import io.github.drumber.kitsune.data.source.jsonapi.library.LibraryEntryPagingDataSource
import io.github.drumber.kitsune.data.utils.InvalidatingPagingSourceFactory
import kotlinx.coroutines.flow.map

class LibraryPagingRepository(
    private val localLibraryDataSource: LibraryLocalDataSource,
    private val remoteLibraryDataSource: LibraryNetworkDataSource,
    private val apolloLibraryDataSource: LibraryApolloDataSource
) {

    private val libraryEntriesPagingSourceFactory =
        InvalidatingPagingSourceFactory<Int, LocalLibraryEntry, LibraryEntryFilter> { filter ->
            localLibraryDataSource.getLibraryEntriesByKindAndStatusAsPagingSource(
                kind = filter.kind,
                status = filter.libraryStatus.map { it.toLocalLibraryStatus() }
            )
        }

    private val libraryEntriesWithNextUnitPagingSourceFactory =
        InvalidatingPagingSourceFactory<Int, LocalLibraryEntryWithModificationAndNextMediaUnit, LibraryFilterOptions> { filter ->
            localLibraryDataSource.getLibraryEntriesWithModificationAndNextUnitAsPagingSource(filter)
        }

    fun invalidateAll() {
        libraryEntriesPagingSourceFactory.invalidate()
        libraryEntriesWithNextUnitPagingSourceFactory.invalidate()
    }

    @OptIn(ExperimentalPagingApi::class)
    fun libraryEntriesPager(pageSize: Int, filter: LibraryEntryFilter) = Pager(
        config = PagingConfig(
            pageSize = pageSize,
            maxSize = Repository.MAX_CACHED_ITEMS
        ),
        remoteMediator = LibraryEntryRemoteMediator(
            filter,
            pageSize,
            remoteLibraryDataSource,
            localLibraryDataSource
        ),
        pagingSourceFactory = { libraryEntriesPagingSourceFactory.createPagingSource(filter) }
    ).flow.map { pagingData ->
        pagingData.map { it.toLibraryEntry() }
    }

    fun searchLibraryEntriesPager(pageSize: Int, filter: Filter) = Pager(
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

    @OptIn(ExperimentalPagingApi::class)
    fun libraryEntriesWithNextMediaUnitPager(pageSize: Int, filter: LibraryFilterOptions) = Pager(
        config = PagingConfig(
            pageSize = pageSize,
            maxSize = Repository.MAX_CACHED_ITEMS
        ),
        remoteMediator = LibraryEntryWithNextMediaUnitRemoteMediator(
            filter,
            pageSize,
            apolloLibraryDataSource,
            localLibraryDataSource
        ),
        pagingSourceFactory = {
            localLibraryDataSource.getLibraryEntriesWithModificationAndNextUnitAsPagingSource(
                filter
            )
        }
    ).flow.map { pagingData ->
        pagingData.map { it.toLibraryEntryWithModificationAndNextUnit() }
    }
}