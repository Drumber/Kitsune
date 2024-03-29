package io.github.drumber.kitsune.domain.repository

import androidx.paging.ExperimentalPagingApi
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingSource
import io.github.drumber.kitsune.constants.Repository
import io.github.drumber.kitsune.domain.database.LibraryEntryDao
import io.github.drumber.kitsune.domain.database.LocalDatabase
import io.github.drumber.kitsune.domain.model.database.LocalLibraryEntry
import io.github.drumber.kitsune.domain.model.ui.library.LibraryEntryFilter
import io.github.drumber.kitsune.domain.model.ui.library.LibraryEntryKind
import io.github.drumber.kitsune.domain.paging.LibraryEntriesPagingDataSource
import io.github.drumber.kitsune.domain.paging.LibraryEntriesRemoteMediator
import io.github.drumber.kitsune.domain.service.Filter
import io.github.drumber.kitsune.domain.service.library.LibraryEntriesService
import java.util.concurrent.CopyOnWriteArrayList

class LibraryEntriesRepository(
    private val service: LibraryEntriesService,
    private val db: LocalDatabase
) {

    @OptIn(ExperimentalPagingApi::class)
    fun libraryEntries(pageSize: Int, filter: LibraryEntryFilter) = Pager(
        config = PagingConfig(
            pageSize = pageSize,
            maxSize = Repository.MAX_CACHED_ITEMS
        ),
        remoteMediator = LibraryEntriesRemoteMediator(filter.pageSize(pageSize), service, db),
        pagingSourceFactory = { invalidatingPagingSourceFactory.createPagingSource(filter) }
    ).flow

    fun searchLibraryEntries(pageSize: Int, filter: Filter) = Pager(
        config = PagingConfig(
            pageSize = pageSize,
            maxSize = Repository.MAX_CACHED_ITEMS
        ),
        pagingSourceFactory = {
            LibraryEntriesPagingDataSource(
                service,
                filter.pageLimit(pageSize)
            )
        }
    ).flow

    private val invalidatingPagingSourceFactory = InvalidatingPagingSourceFactory {
        db.libraryEntryDao().getLibraryEntriesByFilter(it)
    }

    fun invalidatePagingSources() {
        invalidatingPagingSourceFactory.invalidate()
    }

    /**
     * Modified version of [androidx.paging.InvalidatingPagingSourceFactory] accepting a filter object.
     */
    private class InvalidatingPagingSourceFactory<Key : Any, Value : Any>(
        private val pagingSourceFactory: (filter: LibraryEntryFilter) -> PagingSource<Key, Value>
    ) {

        private val pagingSources = CopyOnWriteArrayList<PagingSource<Key, Value>>()

        fun createPagingSource(filter: LibraryEntryFilter): PagingSource<Key, Value> {
            return pagingSourceFactory(filter).also { pagingSources.add(it) }
        }

        fun invalidate() {
            for (pagingSource in pagingSources) {
                if (!pagingSource.invalid) {
                    pagingSource.invalidate()
                }
            }

            pagingSources.removeAll { it.invalid }
        }
    }

}

private fun LibraryEntryDao.getLibraryEntriesByFilter(
    filter: LibraryEntryFilter
): PagingSource<Int, LocalLibraryEntry> {
    val hasStatus = filter.libraryStatus.isNotEmpty()
    val kind = filter.kind
    return when {
        kind == LibraryEntryKind.Anime && hasStatus -> getAnimeLibraryEntry(filter.libraryStatus)
        kind == LibraryEntryKind.Anime && !hasStatus -> getAnimeLibraryEntry()
        kind == LibraryEntryKind.Manga && hasStatus -> getMangaLibraryEntry(filter.libraryStatus)
        kind == LibraryEntryKind.Manga && !hasStatus -> getMangaLibraryEntry()
        kind == LibraryEntryKind.All && hasStatus -> getAllLibraryEntry(filter.libraryStatus)
        else -> getAllLibraryEntry()
    }
}
