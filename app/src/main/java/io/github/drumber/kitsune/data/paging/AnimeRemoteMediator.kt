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
    val animeDao = database.animeDao()
    val remoteKeyDao = database.remoteKeys()

    override suspend fun load(loadType: LoadType, state: PagingState<Int, Anime>): MediatorResult {
        return try {
            val loadKey = when (loadType) {
                LoadType.REFRESH -> Kitsu.DEFAULT_PAGE_OFFSET
                LoadType.PREPEND -> return MediatorResult.Success(endOfPaginationReached = true)
                LoadType.APPEND -> {
                    val remoteKey = database.withTransaction {
                        remoteKeyDao.remoteKeyByQuery(filter.toQueryString(true))
                    }

                    if (remoteKey.nextPageKey == null) {
                        return MediatorResult.Success(endOfPaginationReached = true)
                    }

                    remoteKey.nextPageKey
                }
            }

            val response = service.allAnime(filter.pageOffset(loadKey).options)
            val page = response.links?.toPage()

            database.withTransaction {
                if(loadType == LoadType.REFRESH) {
                    animeDao.clearAllAnime()
                    remoteKeyDao.deleteByQuery(filter.toQueryString(true))
                }

                val data = response.get() ?: throw ReceivedDataException("Received data is 'null'.")
                remoteKeyDao.insert(RemoteKey(filter.toQueryString(true), page?.prev, page?.next))
                animeDao.insertAll(data)
            }

            MediatorResult.Success(endOfPaginationReached = page?.next == null)
        } catch (e: Exception) {
            MediatorResult.Error(e)
        }
    }

    override suspend fun initialize(): InitializeAction {
        return InitializeAction.LAUNCH_INITIAL_REFRESH
    }

}