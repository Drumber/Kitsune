package io.github.drumber.kitsune.data.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import io.github.drumber.kitsune.data.paging.AnimePagingDataSource
import io.github.drumber.kitsune.data.service.AnimeService
import io.github.drumber.kitsune.data.service.Filter

class AnimeRepository(private val service: AnimeService) {

    fun animeCollection(pageSize: Int, filter: Filter) = Pager(
        config = PagingConfig(pageSize),
        pagingSourceFactory = { AnimePagingDataSource(service, filter.pageLimit(pageSize)) }
    ).flow

}