package io.github.drumber.kitsune.data.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.map
import io.github.drumber.kitsune.constants.Repository
import io.github.drumber.kitsune.data.mapper.MediaMapper.toCasting
import io.github.drumber.kitsune.data.source.jsonapi.media.CastingNetworkDataSource
import io.github.drumber.kitsune.data.source.jsonapi.media.CastingPagingDataSource
import io.github.drumber.kitsune.data.common.Filter
import kotlinx.coroutines.flow.map

class CastingRepository(
    private val castingNetworkDataSource: CastingNetworkDataSource
) {

    fun castingPager(filter: Filter, pageSize: Int) = Pager(
        config = PagingConfig(
            pageSize = pageSize,
            maxSize = Repository.MAX_CACHED_ITEMS
        ),
        pagingSourceFactory = {
            CastingPagingDataSource(castingNetworkDataSource, filter.pageLimit(pageSize))
        }
    ).flow.map { pagingData ->
        pagingData.map { it.toCasting() }
    }
}