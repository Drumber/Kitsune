package io.github.drumber.kitsune.data.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import io.github.drumber.kitsune.constants.Repository
import io.github.drumber.kitsune.data.paging.AnimePagingDataSource
import io.github.drumber.kitsune.data.paging.RequestType
import io.github.drumber.kitsune.data.service.Filter
import io.github.drumber.kitsune.data.service.anime.AnimeService

class AnimeRepository(private val service: AnimeService) {

    fun animeCollection(pageSize: Int, filter: Filter, requestType: RequestType = RequestType.ALL) = Pager(
        config = PagingConfig(
            pageSize = pageSize,
            maxSize = Repository.MAX_CACHED_ITEMS
        ),
        pagingSourceFactory = { AnimePagingDataSource(service, filter.pageLimit(pageSize), requestType) }
    ).flow

}