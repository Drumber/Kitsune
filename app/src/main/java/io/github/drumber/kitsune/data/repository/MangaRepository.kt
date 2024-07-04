package io.github.drumber.kitsune.data.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.map
import io.github.drumber.kitsune.constants.Repository
import io.github.drumber.kitsune.data.mapper.MediaMapper.toManga
import io.github.drumber.kitsune.data.presentation.model.media.Manga
import io.github.drumber.kitsune.data.source.network.media.MangaNetworkDataSource
import io.github.drumber.kitsune.data.source.network.media.MangaPagingDataSource
import io.github.drumber.kitsune.data.source.network.media.TrendingMangaPagingDataSource
import io.github.drumber.kitsune.domain_old.service.Filter
import kotlinx.coroutines.flow.map

class MangaRepository(
    private val mangaNetworkDataSource: MangaNetworkDataSource
) {

    suspend fun getAllManga(filter: Filter): List<Manga>? {
        return mangaNetworkDataSource.getAllManga(filter).data?.map { it.toManga() }
    }

    suspend fun getTrending(filter: Filter): List<Manga>? {
        return mangaNetworkDataSource.getTrending(filter).data?.map { it.toManga() }
    }

    suspend fun getManga(id: String, filter: Filter): Manga? {
        return mangaNetworkDataSource.getManga(id, filter)?.toManga()
    }

    fun mangaPager(filter: Filter, pageSize: Int) = Pager(
        config = PagingConfig(
            pageSize = pageSize,
            maxSize = Repository.MAX_CACHED_ITEMS
        ),
        pagingSourceFactory = {
            MangaPagingDataSource(mangaNetworkDataSource, filter.pageLimit(pageSize))
        }
    ).flow.map { pagingData ->
        pagingData.map { it.toManga() }
    }

    fun trendingMangaPager(filter: Filter, pageSize: Int) = Pager(
        config = PagingConfig(
            pageSize = pageSize,
            maxSize = Repository.MAX_CACHED_ITEMS
        ),
        pagingSourceFactory = {
            TrendingMangaPagingDataSource(mangaNetworkDataSource, filter.pageLimit(pageSize))
        }
    ).flow.map { pagingData ->
        pagingData.map { it.toManga() }
    }
}