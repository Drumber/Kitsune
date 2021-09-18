package io.github.drumber.kitsune.data.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import io.github.drumber.kitsune.constants.Repository
import io.github.drumber.kitsune.data.paging.MangaPagingDataSource
import io.github.drumber.kitsune.data.service.Filter
import io.github.drumber.kitsune.data.service.manga.MangaService

class MangaRepository(private val service: MangaService) {

    fun mangaCollection(pageSize: Int, filter: Filter) = Pager(
        config = PagingConfig(
            pageSize = pageSize,
            maxSize = Repository.MAX_CACHED_ITEMS
        ),
        pagingSourceFactory = { MangaPagingDataSource(service, filter.pageLimit(pageSize)) }
    ).flow

}