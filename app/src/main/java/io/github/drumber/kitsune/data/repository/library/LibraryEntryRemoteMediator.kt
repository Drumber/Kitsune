package io.github.drumber.kitsune.data.repository.library

import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import io.github.drumber.kitsune.constants.Kitsu
import io.github.drumber.kitsune.data.common.exception.NoDataException
import io.github.drumber.kitsune.data.mapper.LibraryMapper.toLocalLibraryEntry
import io.github.drumber.kitsune.data.mapper.LibraryMapper.toLocalLibraryStatus
import io.github.drumber.kitsune.data.presentation.model.library.LibraryEntryFilter
import io.github.drumber.kitsune.data.source.local.LocalDatabase
import io.github.drumber.kitsune.data.source.local.library.LibraryLocalDataSource
import io.github.drumber.kitsune.data.source.local.library.model.LocalLibraryEntry
import io.github.drumber.kitsune.data.source.local.library.model.RemoteKeyEntity
import io.github.drumber.kitsune.data.source.local.library.model.RemoteKeyType
import io.github.drumber.kitsune.data.source.network.library.LibraryNetworkDataSource
import io.github.drumber.kitsune.util.logD
import io.github.drumber.kitsune.util.parseUtcDate

@OptIn(ExperimentalPagingApi::class)
class LibraryEntryRemoteMediator(
    private val filter: LibraryEntryFilter,
    private val networkDataSource: LibraryNetworkDataSource,
    private val localDataSource: LibraryLocalDataSource
) : RemoteMediator<Int, LocalLibraryEntry>() {

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
                    var nextKey = remoteKeys?.nextPageKey?.toIntOrNull()
                        ?: return MediatorResult.Success(endOfPaginationReached = remoteKeys != null)

                    state.pages.lastOrNull()?.prevKey?.let { prevPage ->
                        // last page is equal or greater than reported next page: RemoteKey is out of sync
                        if (prevPage >= nextKey) {
                            localDataSource.deleteRemoteKeyByResourceId(remoteKeys.resourceId, remoteKeys.remoteKeyType)
                            getRemoteKeyForLastItem(state)?.nextPageKey?.toIntOrNull()?.let {
                                nextKey = it
                            } ?: return MediatorResult.Success(endOfPaginationReached = false)
                        }
                    }
                    nextKey
                }
            }

            val pageData = networkDataSource.getAllLibraryEntries(
                filter.buildFilter().pageOffset(pageOffset)
            )
            val data = pageData.data?.map { it.toLocalLibraryEntry() }
                ?: throw NoDataException("Received data is 'null'.")

            localDataSource.runDatabaseTransaction {
                // only clear database on REFRESH
                if (loadType == LoadType.REFRESH) {
                    logD("Clearing filtered library entries and corresponding remote keys from database.")
                    clearLibraryEntriesForFilterIgnoringNewerLibraryEntries(filter, data)
                }

                data.forEach { libraryEntry ->
                    localDataSource.insertLibraryEntryIfUpdatedAtIsNewer(libraryEntry)
                }

                val remoteKeys = data.map {
                    RemoteKeyEntity(it.id, RemoteKeyType.LibraryEntry, pageData.prev?.toString(), pageData.next?.toString())
                }
                remoteKeyDao().insertALl(remoteKeys)
            }

            MediatorResult.Success(endOfPaginationReached = pageData.next == null)
        } catch (e: Exception) {
            MediatorResult.Error(e)
        }
    }

    private suspend fun LocalDatabase.clearLibraryEntriesForFilterIgnoringNewerLibraryEntries(
        filter: LibraryEntryFilter,
        data: List<LocalLibraryEntry>
    ) {
        // clear all library entries in database with the selected kind and status
        val libraryEntriesToBeCleared = localDataSource.getLibraryEntriesByKindAndStatus(
            filter.kind,
            filter.libraryStatus.map { it.toLocalLibraryStatus() }
        ).filter { existingLibraryEntry ->
            // do not clear library entries from database that are newer than the ones received
            val updatedAtOfExistingEntry = existingLibraryEntry.updatedAt
            val updatedAtOfNewEntry = data.find { it.id == existingLibraryEntry.id }?.updatedAt
            if (updatedAtOfExistingEntry.isNullOrBlank() || updatedAtOfNewEntry.isNullOrBlank())
                return@filter true

            val existingEntryUpdateTime = updatedAtOfExistingEntry.parseUtcDate()?.time
            val newEntryUpdateTime = updatedAtOfNewEntry.parseUtcDate()?.time
            return@filter existingEntryUpdateTime == null || newEntryUpdateTime == null ||
                    existingEntryUpdateTime <= newEntryUpdateTime
        }

        libraryEntryDao().deleteAll(libraryEntriesToBeCleared)
        val remoteKeyIdsToClear = libraryEntriesToBeCleared.map { it.id }
        remoteKeyDao().deleteAllByResourceId(remoteKeyIdsToClear, RemoteKeyType.LibraryEntry)
    }

    private suspend fun getRemoteKeyForLastItem(state: PagingState<Int, LocalLibraryEntry>): RemoteKeyEntity? {
        // Get the last page that was retrieved, that contained items.
        // From that last page, get the last item that has a valid remote key.
        return state.pages.lastOrNull { it.data.isNotEmpty() }?.data
            ?.lastOrNull { libraryEntry ->
                localDataSource.getRemoteKeyByResourceId(
                    libraryEntry.id,
                    RemoteKeyType.LibraryEntry
                ) != null
            }
            ?.let { libraryEntry ->
                // Get the remote keys of the last item retrieved
                localDataSource.getRemoteKeyByResourceId(
                    libraryEntry.id,
                    RemoteKeyType.LibraryEntry
                )
            }
    }

    override suspend fun initialize(): InitializeAction {
        return InitializeAction.LAUNCH_INITIAL_REFRESH
    }

}