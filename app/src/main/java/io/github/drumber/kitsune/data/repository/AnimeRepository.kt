package io.github.drumber.kitsune.data.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.map
import io.github.drumber.kitsune.constants.Repository
import io.github.drumber.kitsune.data.mapper.MediaMapper.toAnime
import io.github.drumber.kitsune.data.presentation.model.media.Anime
import io.github.drumber.kitsune.data.source.network.media.AnimeNetworkDataSource
import io.github.drumber.kitsune.data.source.network.media.AnimePagingDataSource
import io.github.drumber.kitsune.data.source.network.media.TrendingAnimePagingDataSource
import io.github.drumber.kitsune.data.common.Filter
import kotlinx.coroutines.flow.map

class AnimeRepository(
    private val animeNetworkDataSource: AnimeNetworkDataSource
) {

    suspend fun getAllAnime(filter: Filter): List<Anime>? {
        return animeNetworkDataSource.getAllAnime(filter).data?.map { it.toAnime() }
    }

    suspend fun getTrending(filter: Filter): List<Anime>? {
        return animeNetworkDataSource.getTrending(filter).data?.map { it.toAnime() }
    }

    suspend fun getAnime(id: String, filter: Filter): Anime? {
        return animeNetworkDataSource.getAnime(id, filter)?.toAnime()
    }

    suspend fun getLanguages(id: String): List<String> {
        return animeNetworkDataSource.getLanguages(id)
    }

    fun animePager(filter: Filter, pageSize: Int) = Pager(
        config = PagingConfig(
            pageSize = pageSize,
            maxSize = Repository.MAX_CACHED_ITEMS
        ),
        pagingSourceFactory = {
            AnimePagingDataSource(animeNetworkDataSource, filter.pageLimit(pageSize))
        }
    ).flow.map { pagingData ->
        pagingData.map { it.toAnime() }
    }

    fun trendingAnimePager(filter: Filter, pageSize: Int) = Pager(
        config = PagingConfig(
            pageSize = pageSize,
            maxSize = Repository.MAX_CACHED_ITEMS
        ),
        pagingSourceFactory = {
            TrendingAnimePagingDataSource(animeNetworkDataSource, filter.pageLimit(pageSize))
        }
    ).flow.map { pagingData ->
        pagingData.map { it.toAnime() }
    }
}