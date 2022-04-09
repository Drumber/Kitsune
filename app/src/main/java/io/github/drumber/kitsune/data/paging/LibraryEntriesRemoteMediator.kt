package io.github.drumber.kitsune.data.paging

import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import androidx.room.withTransaction
import io.github.drumber.kitsune.constants.Kitsu
import io.github.drumber.kitsune.data.model.RemoteKey
import io.github.drumber.kitsune.data.model.RemoteKeyType
import io.github.drumber.kitsune.data.model.library.LibraryEntry
import io.github.drumber.kitsune.data.model.library.LibraryEntryFilter
import io.github.drumber.kitsune.data.model.toPage
import io.github.drumber.kitsune.data.room.ResourceDatabase
import io.github.drumber.kitsune.data.service.library.LibraryEntriesService
import io.github.drumber.kitsune.exception.ReceivedDataException

@OptIn(ExperimentalPagingApi::class)
class LibraryEntriesRemoteMediator(
    private val filter: LibraryEntryFilter,
    private val service: LibraryEntriesService,
    private val database: ResourceDatabase,
) : RemoteMediator<Int, LibraryEntry>() {
    private val libraryEntryDao = database.libraryEntryDao()
    private val remoteKeyDao = database.remoteKeys()

    /**
     * Implementation based on android paging example from google code labs:
     * [https://github.com/googlecodelabs/android-paging/blob/78d231f6fbe9bf1326993362e1d08f823bef5ea2/app/src/main/java/com/example/android/codelabs/paging/data/GithubRemoteMediator.kt]
     */
    override suspend fun load(
        loadType: LoadType,
        state: PagingState<Int, LibraryEntry>
    ): MediatorResult {
        val pageSize = state.config.pageSize
        return try {
            val pageOffset = when (loadType) {
                LoadType.REFRESH -> {
                    val remoteKeys = getRemoteKeyClosestToCurrentPosition(state)
                    remoteKeys?.nextPageKey?.minus(pageSize) ?: Kitsu.DEFAULT_PAGE_OFFSET
                }
                LoadType.PREPEND -> {
                    val remoteKeys = getRemoteKeyForFirstItem(state)
                    // If remoteKeys is null, that means the refresh result is not in the database yet.
                    // We can return Success with `endOfPaginationReached = false` because Paging
                    // will call this method again if RemoteKeys becomes non-null.
                    // If remoteKeys is NOT NULL but its prevKey is null, that means we've reached
                    // the end of pagination for prepend.
                    val prevKey = remoteKeys?.prevPageKey
                        ?: return MediatorResult.Success(endOfPaginationReached = remoteKeys != null)
                    prevKey
                }
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
                // only clear database on REFRESH and if the full library was requested (is not filtered)
                // otherwise library entries won't be available for offline use
                if (loadType == LoadType.REFRESH && !filter.isFiltered()) {
                    libraryEntryDao.clearLibraryEntries()
                    remoteKeyDao.clearRemoteKeys(RemoteKeyType.LibraryEntry)
                }

                val data = response.get() ?: throw ReceivedDataException("Received data is 'null'.")

                val remoteKeys = data.map {
                    RemoteKey(it.id, RemoteKeyType.LibraryEntry, page?.prev, page?.next)
                }

                remoteKeyDao.insertALl(remoteKeys)
                libraryEntryDao.insertAll(data)
            }

            MediatorResult.Success(endOfPaginationReached = endReached)
        } catch (e: Exception) {
            MediatorResult.Error(e)
        }
    }

    private suspend fun getRemoteKeyForLastItem(state: PagingState<Int, LibraryEntry>): RemoteKey? {
        // Get the last page that was retrieved, that contained items.
        // From that last page, get the last item
        return state.pages.lastOrNull { it.data.isNotEmpty() }?.data?.lastOrNull()
            ?.let { libraryEntry ->
                // Get the remote keys of the last item retrieved
                remoteKeyDao.remoteKeyByResourceId(libraryEntry.id, RemoteKeyType.LibraryEntry)
            }
    }

    private suspend fun getRemoteKeyForFirstItem(state: PagingState<Int, LibraryEntry>): RemoteKey? {
        // Get the first page that was retrieved, that contained items.
        // From that first page, get the first item
        return state.pages.firstOrNull { it.data.isNotEmpty() }?.data?.firstOrNull()
            ?.let { libraryEntry ->
                // Get the remote keys of the first items retrieved
                remoteKeyDao.remoteKeyByResourceId(libraryEntry.id, RemoteKeyType.LibraryEntry)
            }
    }

    private suspend fun getRemoteKeyClosestToCurrentPosition(
        state: PagingState<Int, LibraryEntry>
    ): RemoteKey? {
        // The paging library is trying to load data after the anchor position
        // Get the item closest to the anchor position
        return state.anchorPosition?.let { position ->
            state.closestItemToPosition(position)?.id?.let { libraryEntryId ->
                remoteKeyDao.remoteKeyByResourceId(libraryEntryId, RemoteKeyType.LibraryEntry)
            }
        }
    }

    override suspend fun initialize(): InitializeAction {
        return InitializeAction.LAUNCH_INITIAL_REFRESH
    }

}