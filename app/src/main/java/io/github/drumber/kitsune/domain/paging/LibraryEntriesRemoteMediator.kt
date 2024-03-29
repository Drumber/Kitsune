package io.github.drumber.kitsune.domain.paging

import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import androidx.room.withTransaction
import io.github.drumber.kitsune.constants.Kitsu
import io.github.drumber.kitsune.domain.database.LocalDatabase
import io.github.drumber.kitsune.domain.mapper.toLocalLibraryEntry
import io.github.drumber.kitsune.domain.model.common.library.LibraryStatus
import io.github.drumber.kitsune.domain.model.database.LocalLibraryEntry
import io.github.drumber.kitsune.domain.model.database.RemoteKeyEntity
import io.github.drumber.kitsune.domain.model.database.RemoteKeyType
import io.github.drumber.kitsune.domain.model.toPage
import io.github.drumber.kitsune.domain.model.ui.library.LibraryEntryFilter
import io.github.drumber.kitsune.domain.model.ui.library.LibraryEntryKind
import io.github.drumber.kitsune.domain.service.library.LibraryEntriesService
import io.github.drumber.kitsune.exception.ReceivedDataException
import io.github.drumber.kitsune.util.logD

@OptIn(ExperimentalPagingApi::class)
class LibraryEntriesRemoteMediator(
    private val filter: LibraryEntryFilter,
    private val service: LibraryEntriesService,
    private val database: LocalDatabase,
) : RemoteMediator<Int, LocalLibraryEntry>() {
    private val libraryEntryDao = database.libraryEntryDao()
    private val remoteKeyDao = database.remoteKeyDao()

    /**
     * Implementation based on android paging example from
     * [Google Code Labs](https://github.com/googlecodelabs/android-paging/blob/78d231f6fbe9bf1326993362e1d08f823bef5ea2/app/src/main/java/com/example/android/codelabs/paging/data/GithubRemoteMediator.kt).
     */
    override suspend fun load(
        loadType: LoadType,
        state: PagingState<Int, LocalLibraryEntry>
    ): MediatorResult {
        return try {
            val pageOffset = when (loadType) {
                LoadType.REFRESH -> Kitsu.DEFAULT_PAGE_OFFSET
                LoadType.PREPEND -> return MediatorResult.Success(endOfPaginationReached = true)
                LoadType.APPEND -> {
                    val remoteKeys = getRemoteKeyForLastItem(state)
                    // If remoteKeys is null, that means the refresh result is not in the database yet.
                    // We can return Success with `endOfPaginationReached = false` because Paging
                    // will call this method again if RemoteKeys becomes non-null.
                    // If remoteKeys is NOT NULL but its prevKey is null, that means we've reached
                    // the end of pagination for append.
                    val nextKey = remoteKeys?.nextPageKey
                        ?: return MediatorResult.Success(endOfPaginationReached = remoteKeys != null)
                    nextKey
                }
            }

            val response = service.allLibraryEntries(
                filter.buildFilter().pageOffset(pageOffset).options
            )
            val page = response.links?.toPage()
            val endReached = page?.next == null

            database.withTransaction {
                // only clear database on REFRESH
                if (loadType == LoadType.REFRESH) {
                    // if no filter is selected (full library is shown), clear full database
                    if (!filter.isFiltered()) {
                        logD("Clearing all library entries and remote keys from database.")
                        libraryEntryDao.clearLibraryEntries()
                        remoteKeyDao.clearRemoteKeys(RemoteKeyType.LibraryEntry)
                    }
                    // otherwise clear all displayed library entries
                    else {
                        logD("Clearing filtered library entries and corresponding remote keys from database.")
                        // if no status filter is selected, we target all status types
                        val targetStatus = filter.libraryStatus.ifEmpty { LibraryStatus.entries }

                        // clear all library entries in database with the selected kind and status
                        val libraryEntriesToBeCleared = when (filter.kind) {
                            LibraryEntryKind.Anime -> libraryEntryDao.getAnimeLibraryEntryByStatus(
                                targetStatus
                            )

                            LibraryEntryKind.Manga -> libraryEntryDao.getMangaLibraryEntryByStatus(
                                targetStatus
                            )

                            else -> libraryEntryDao.getAllLibraryEntryByStatus(targetStatus)
                        }

                        libraryEntryDao.deleteAll(libraryEntriesToBeCleared)
                        libraryEntriesToBeCleared.forEach { libraryEntry ->
                            remoteKeyDao.deleteByResourceId(
                                libraryEntry.id,
                                RemoteKeyType.LibraryEntry
                            )
                        }
                    }
                }

                val data = response.get()?.map { it.toLocalLibraryEntry() }
                    ?: throw ReceivedDataException("Received data is 'null'.")

                val remoteKeys = data.map {
                    RemoteKeyEntity(it.id, RemoteKeyType.LibraryEntry, page?.prev, page?.next)
                }

                remoteKeyDao.insertALl(remoteKeys)
                libraryEntryDao.insertAll(data)
            }

            MediatorResult.Success(endOfPaginationReached = endReached)
        } catch (e: Exception) {
            MediatorResult.Error(e)
        }
    }

    private suspend fun getRemoteKeyForLastItem(state: PagingState<Int, LocalLibraryEntry>): RemoteKeyEntity? {
        // Get the last page that was retrieved, that contained items.
        // From that last page, get the last item
        return state.pages.lastOrNull { it.data.isNotEmpty() }?.data?.lastOrNull()
            ?.let { libraryEntry ->
                // Get the remote keys of the last item retrieved
                remoteKeyDao.remoteKeyByResourceId(libraryEntry.id, RemoteKeyType.LibraryEntry)
            }
    }

    override suspend fun initialize(): InitializeAction {
        return InitializeAction.LAUNCH_INITIAL_REFRESH
    }

}