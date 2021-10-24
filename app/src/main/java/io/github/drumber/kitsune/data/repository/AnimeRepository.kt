package io.github.drumber.kitsune.data.repository

import androidx.paging.ExperimentalPagingApi
import androidx.paging.Pager
import androidx.paging.PagingConfig
import io.github.drumber.kitsune.constants.Repository
import io.github.drumber.kitsune.data.paging.AnimeRemoteMediator
import io.github.drumber.kitsune.data.paging.RequestType
import io.github.drumber.kitsune.data.room.ResourceDatabase
import io.github.drumber.kitsune.data.service.Filter
import io.github.drumber.kitsune.data.service.anime.AnimeService

class AnimeRepository(private val service: AnimeService, private val db: ResourceDatabase) {

    @OptIn(ExperimentalPagingApi::class)
    fun animeCollection(pageSize: Int, filter: Filter, requestType: RequestType = RequestType.ALL) = Pager(
        config = PagingConfig(
            pageSize = pageSize,
            maxSize = Repository.MAX_CACHED_ITEMS
        ),
        remoteMediator = AnimeRemoteMediator(filter.pageLimit(pageSize), service, db, requestType),
        pagingSourceFactory = { db.animeDao().getAnime() }
    ).flow

}