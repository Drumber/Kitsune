package io.github.drumber.kitsune.data.repository

import androidx.paging.PagingSource
import io.github.drumber.kitsune.data.mapper.MediaMapper.toManga
import io.github.drumber.kitsune.data.presentation.model.media.Manga
import io.github.drumber.kitsune.data.source.network.media.MangaNetworkDataSource
import io.github.drumber.kitsune.data.source.network.media.MangaPagingDataSource
import io.github.drumber.kitsune.data.source.network.media.TrendingMangaPagingDataSource
import io.github.drumber.kitsune.data.source.network.media.model.NetworkManga
import io.github.drumber.kitsune.domain_old.service.Filter

class MangaRepository(
    private val mangaNetworkDataSource: MangaNetworkDataSource
) {

    suspend fun getManga(id: String, filter: Filter): Manga? {
        return mangaNetworkDataSource.getManga(id, filter)?.toManga()
    }

    fun mangaPagingSource(filter: Filter): PagingSource<Int, NetworkManga> {
        return MangaPagingDataSource(mangaNetworkDataSource, filter)
    }

    fun trendingMangaPagingSource(filter: Filter): PagingSource<Int, NetworkManga> {
        return TrendingMangaPagingDataSource(mangaNetworkDataSource, filter)
    }
}