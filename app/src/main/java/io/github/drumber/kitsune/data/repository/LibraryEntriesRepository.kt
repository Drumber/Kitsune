package io.github.drumber.kitsune.data.repository

import androidx.paging.ExperimentalPagingApi
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingSource
import io.github.drumber.kitsune.constants.Repository
import io.github.drumber.kitsune.data.model.library.LibraryEntry
import io.github.drumber.kitsune.data.model.library.LibraryEntryFilter
import io.github.drumber.kitsune.data.model.library.LibraryEntryKind
import io.github.drumber.kitsune.data.paging.LibraryEntriesRemoteMediator
import io.github.drumber.kitsune.data.room.LibraryEntryDao
import io.github.drumber.kitsune.data.room.ResourceDatabase
import io.github.drumber.kitsune.data.service.library.LibraryEntriesService

class LibraryEntriesRepository(private val service: LibraryEntriesService, private val db: ResourceDatabase) {

    @OptIn(ExperimentalPagingApi::class)
    fun libraryEntries(pageSize: Int, filter: LibraryEntryFilter) = Pager(
        config = PagingConfig(
            pageSize = pageSize,
            maxSize = Repository.MAX_CACHED_ITEMS
        ),
        remoteMediator = LibraryEntriesRemoteMediator(filter.pageSize(pageSize), service, db),
        pagingSourceFactory = { db.libraryEntryDao().getLibraryEntriesByFilter(filter) }
    ).flow

}

private fun LibraryEntryDao.getLibraryEntriesByFilter(filter: LibraryEntryFilter): PagingSource<Int, LibraryEntry> {
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
