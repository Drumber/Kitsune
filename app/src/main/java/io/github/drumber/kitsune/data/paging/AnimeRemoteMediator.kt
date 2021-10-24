package io.github.drumber.kitsune.data.paging

import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import androidx.room.withTransaction
import io.github.drumber.kitsune.constants.Kitsu
import io.github.drumber.kitsune.data.model.RemoteKey
import io.github.drumber.kitsune.data.model.resource.Anime
import io.github.drumber.kitsune.data.model.toPage
import io.github.drumber.kitsune.data.room.ResourceDatabase
import io.github.drumber.kitsune.data.service.Filter
import io.github.drumber.kitsune.data.service.anime.AnimeService
import io.github.drumber.kitsune.exception.ReceivedDataException

@OptIn(ExperimentalPagingApi::class)
class AnimeRemoteMediator(
    private val filter: Filter,
    private val service: AnimeService,
    private val database: ResourceDatabase,
    private val requestType: RequestType = RequestType.ALL
) : RemoteMediator<Int, Anime>() {
    private val animeDao = database.animeDao()
    private val remoteKeyDao = database.remoteKeys()

    override suspend fun load(loadType: LoadType, state: PagingState<Int, Anime>): MediatorResult {
        return try {
            val pageOffset = when (loadType) {
                LoadType.REFRESH -> {
                    val remoteKey = getRemoteKeyClosestToCurrentPosition(state)
                    remoteKey?.nextPageKey?.minus(1) ?: Kitsu.DEFAULT_PAGE_OFFSET
                }
                LoadType.PREPEND -> return MediatorResult.Success(endOfPaginationReached = true)
                LoadType.APPEND -> {
                    val remoteKey = getRemoteKeyForLastItem(state)

                    if (remoteKey == null) {
                        state.config.pageSize
                    } else {
                        remoteKey.nextPageKey ?: return MediatorResult.Success(endOfPaginationReached = true)
                    }
                }
            }

            val response = service.allAnime(filter.pageOffset(pageOffset).options)
            val page = response.links?.toPage()
            val endReached = page?.next == null

            database.withTransaction {
                if(loadType == LoadType.REFRESH) {
                    animeDao.clearAllAnime()
                    remoteKeyDao.clearRemoteKeys()
                }

                val data = response.get() ?: throw ReceivedDataException("Received data is 'null'.")

                val remoteKeys = data.map {
                    RemoteKey(it.id, page?.prev, page?.next)
                }

                remoteKeyDao.insertALl(remoteKeys)
                animeDao.insertAll(data)
            }

            MediatorResult.Success(endOfPaginationReached = endReached)
        } catch (e: Exception) {
            MediatorResult.Error(e)
        }
    }

    private suspend fun getRemoteKeyForLastItem(state: PagingState<Int, Anime>): RemoteKey? {
        return state.lastItemOrNull()?.let { anime ->
            database.withTransaction {
                remoteKeyDao.remoteKeyByResourceId(anime.id)
            }
        }
    }

    private suspend fun getRemoteKeyClosestToCurrentPosition(state: PagingState<Int, Anime>): RemoteKey? {
        return state.anchorPosition?.let { position ->
            state.closestItemToPosition(position)?.id?.let { id ->
                database.withTransaction {
                    remoteKeyDao.remoteKeyByResourceId(id)
                }
            }
        }
    }

    override suspend fun initialize(): InitializeAction {
        return InitializeAction.LAUNCH_INITIAL_REFRESH
    }

}