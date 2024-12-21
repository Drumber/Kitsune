package io.github.drumber.kitsune.data.repository.library

import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import io.github.drumber.kitsune.data.common.exception.NoDataException
import io.github.drumber.kitsune.data.common.library.LibraryFilterOptions
import io.github.drumber.kitsune.data.mapper.LibraryMapper.toLocalLibraryEntry
import io.github.drumber.kitsune.data.mapper.graphql.toLibraryEntriesWithModificationAndNextUnit
import io.github.drumber.kitsune.data.mapper.graphql.toLocalNextMediaUnit
import io.github.drumber.kitsune.data.source.graphql.library.LibraryApolloDataSource
import io.github.drumber.kitsune.data.source.local.library.LibraryLocalDataSource
import io.github.drumber.kitsune.data.source.local.library.model.LocalLibraryEntryWithModificationAndNextMediaUnit
import io.github.drumber.kitsune.data.source.local.library.model.RemoteKeyEntity
import io.github.drumber.kitsune.data.source.local.library.model.RemoteKeyType

@OptIn(ExperimentalPagingApi::class)
class LibraryEntryWithNextMediaUnitRemoteMediator(
    private val filter: LibraryFilterOptions,
    private val pageSize: Int,
    private val apolloDataSource: LibraryApolloDataSource,
    private val localDataSource: LibraryLocalDataSource
) : RemoteMediator<Int, LocalLibraryEntryWithModificationAndNextMediaUnit>() {

    override suspend fun load(
        loadType: LoadType,
        state: PagingState<Int, LocalLibraryEntryWithModificationAndNextMediaUnit>
    ): MediatorResult {
        return try {
            val pageOffset = when (loadType) {
                LoadType.REFRESH -> null
                LoadType.PREPEND -> return MediatorResult.Success(endOfPaginationReached = true)
                LoadType.APPEND -> {
                    val remoteKeys = getRemoteKeyForLastItem(state)
                    val nextKey = remoteKeys?.nextPageKey
                        ?: return MediatorResult.Success(endOfPaginationReached = remoteKeys != null)
                    nextKey
                }
            }

            val pageData = apolloDataSource.getLibraryEntriesWithNextUnit(
                cursor = pageOffset,
                pageSize = pageSize,
                filter = filter
            ) ?: throw NoDataException("Received data is 'null'.")

            val pageInfo = pageData.pageInfo
            val data = pageData.toLibraryEntriesWithModificationAndNextUnit()
                ?: throw NoDataException("Received data is 'null'.")

            localDataSource.runDatabaseTransaction {
                if (loadType == LoadType.REFRESH) {
                    // TODO: clear old library entries
                }

                data.forEach {
                    localDataSource.insertLibraryEntryIfUpdatedAtIsNewer(it.libraryEntryWithModification.libraryEntry.toLocalLibraryEntry())
                    if (it.nextUnit != null) {
                        localDataSource.insertNextMediaUnit(it.nextUnit.toLocalNextMediaUnit(it.libraryEntryWithModification.id))
                    }
                }

                val remoteKeys = data
                    .map { it.libraryEntryWithModification.libraryEntry.toLocalLibraryEntry() }
                    .map {
                        RemoteKeyEntity(
                            it.id,
                            RemoteKeyType.LibraryEntryWithNextMediaUnit,
                            pageInfo.startCursor,
                            pageInfo.endCursor
                        )
                    }
                remoteKeyDao().insertALl(remoteKeys)
            }

            MediatorResult.Success(endOfPaginationReached = !pageInfo.hasNextPage)
        } catch (e: Exception) {
            MediatorResult.Error(e)
        }
    }

    private suspend fun getRemoteKeyForLastItem(state: PagingState<Int, LocalLibraryEntryWithModificationAndNextMediaUnit>): RemoteKeyEntity? {
        // Get the last page that was retrieved, that contained items.
        // From that last page, get the last item that has a valid remote key.
        return state.pages.lastOrNull { it.data.isNotEmpty() }?.data
            ?.lastOrNull { libraryEntryWrapper ->
                localDataSource.getRemoteKeyByResourceId(
                    libraryEntryWrapper.libraryEntry.id,
                    RemoteKeyType.LibraryEntryWithNextMediaUnit
                ) != null
            }
            ?.let { libraryEntryWrapper ->
                // Get the remote keys of the last item retrieved
                localDataSource.getRemoteKeyByResourceId(
                    libraryEntryWrapper.libraryEntry.id,
                    RemoteKeyType.LibraryEntryWithNextMediaUnit
                )
            }
    }

    override suspend fun initialize(): InitializeAction {
        return InitializeAction.LAUNCH_INITIAL_REFRESH
    }
}