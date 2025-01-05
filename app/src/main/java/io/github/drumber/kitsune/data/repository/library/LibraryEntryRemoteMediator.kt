package io.github.drumber.kitsune.data.repository.library

import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import io.github.drumber.kitsune.shared.Kitsu
import io.github.drumber.kitsune.data.exception.NoDataException
import io.github.drumber.kitsune.data.common.library.LibraryFilterOptions
import io.github.drumber.kitsune.data.mapper.LibraryMapper.toLocalLibraryEntry
import io.github.drumber.kitsune.data.mapper.LibraryMapper.toLocalLibraryFilterOptions
import io.github.drumber.kitsune.data.presentation.model.library.LibraryEntryFilter
import io.github.drumber.kitsune.data.presentation.model.library.toLibraryFilterOptions
import io.github.drumber.kitsune.data.source.jsonapi.library.LibraryNetworkDataSource
import io.github.drumber.kitsune.data.source.local.library.LibraryLocalDataSource
import io.github.drumber.kitsune.data.source.local.library.model.LocalLibraryEntry
import io.github.drumber.kitsune.data.source.local.library.model.RemoteKeyEntity
import io.github.drumber.kitsune.data.source.local.mapper.toLocalLibraryStatus
import io.github.drumber.kitsune.shared.parseUtcDate
import io.github.drumber.kitsune.shared.logD

@OptIn(ExperimentalPagingApi::class)
class LibraryEntryRemoteMediator(
    private val filter: LibraryEntryFilter,
    private val pageSize: Int,
    private val networkDataSource: LibraryNetworkDataSource,
    private val localDataSource: LibraryLocalDataSource
) : RemoteMediator<Int, LocalLibraryEntry>() {

    // TODO: Remove after migration to new library filter options
    private val filterOptions = filter.toLibraryFilterOptions()

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
                    val remoteKey = getRemoteKeyForLastItem(state)
                    // If remoteKeys is null, that means the refresh result is not in the database yet.
                    // We can return Success with `endOfPaginationReached = false` because Paging
                    // will call this method again if RemoteKeys becomes non-null.
                    // If remoteKeys is NOT NULL but its prevKey is null, that means we've reached
                    // the end of pagination for append.
                    var nextKey = remoteKey?.nextPageKey?.toIntOrNull()
                        ?: return MediatorResult.Success(endOfPaginationReached = remoteKey != null)

                    state.pages.lastOrNull()?.prevKey?.let { prevPage ->
                        // last page is equal or greater than reported next page: RemoteKey is out of sync
                        if (prevPage >= nextKey) {
                            localDataSource.deleteRemoteKey(remoteKey)
                            getRemoteKeyForLastItem(state)?.nextPageKey?.toIntOrNull()?.let {
                                nextKey = it
                            } ?: return MediatorResult.Success(endOfPaginationReached = false)
                        }
                    }
                    nextKey
                }
            }

            val pageData = networkDataSource.getAllLibraryEntries(
                filter.buildFilter().pageLimit(pageSize).pageOffset(pageOffset)
            )
            val data = pageData.data?.map { it.toLocalLibraryEntry() }
                ?: throw NoDataException("Received data is 'null'.")

            localDataSource.runDatabaseTransaction {
                // only clear database on REFRESH
                if (loadType == LoadType.REFRESH) {
                    logD("Clearing filtered library entries and corresponding remote keys from database.")
                    clearLibraryEntriesForFilterIgnoringNewerLibraryEntries(filterOptions, data)
                }

                data.forEach { libraryEntry ->
                    localDataSource.insertLibraryEntryIfUpdatedAtIsNewer(libraryEntry)
                }

                val remoteKeys = data.map {
                    RemoteKeyEntity(
                        resourceId = it.id,
                        filterOptions = filterOptions.toLocalLibraryFilterOptions(),
                        prevPageKey = pageData.prev?.toString(),
                        nextPageKey = pageData.next?.toString()
                    )
                }
                remoteKeyDao().insertALl(remoteKeys)
            }

            MediatorResult.Success(endOfPaginationReached = pageData.next == null)
        } catch (e: Exception) {
            MediatorResult.Error(e)
        }
    }

    private suspend fun io.github.drumber.kitsune.data.source.local.LocalDatabase.clearLibraryEntriesForFilterIgnoringNewerLibraryEntries(
        filter: LibraryFilterOptions,
        data: List<LocalLibraryEntry>
    ) {
        // clear all library entries in database with the selected kind and status
        val libraryEntriesToBeCleared = localDataSource.getLibraryEntriesByKindAndStatus(
            filter.mediaType,
            filter.status?.map { it.toLocalLibraryStatus() } ?: emptyList()
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
        localDataSource.deleteAllRemoteKeysByResourceId(remoteKeyIdsToClear, filter)
    }

    private suspend fun getRemoteKeyForLastItem(state: PagingState<Int, LocalLibraryEntry>): RemoteKeyEntity? {
        // Get the last page that was retrieved, that contained items.
        // From that last page, get the last item that has a valid remote key.
        return state.pages.lastOrNull { it.data.isNotEmpty() }?.data
            ?.lastOrNull { libraryEntry ->
                localDataSource.getRemoteKeyByResourceId(
                    libraryEntry.id,
                    filterOptions
                ) != null
            }
            ?.let { libraryEntry ->
                // Get the remote keys of the last item retrieved
                localDataSource.getRemoteKeyByResourceId(
                    libraryEntry.id,
                    filterOptions
                )
            }
    }

    override suspend fun initialize(): InitializeAction {
        return InitializeAction.LAUNCH_INITIAL_REFRESH
    }

}