package io.github.drumber.kitsune.domain_old.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import io.github.drumber.kitsune.constants.Repository
import io.github.drumber.kitsune.domain_old.paging.CastingPagingDataSource
import io.github.drumber.kitsune.domain_old.service.Filter
import io.github.drumber.kitsune.domain_old.service.production.CastingService

class CastingRepository(private val service: CastingService) {

    fun castingCollection(pageSize: Int, filter: Filter) = Pager(
        config = PagingConfig(
            pageSize = pageSize,
            maxSize = Repository.MAX_CACHED_ITEMS
        ),
        pagingSourceFactory = { CastingPagingDataSource(service, filter.pageLimit(pageSize)) }
    ).flow

}