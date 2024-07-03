package io.github.drumber.kitsune.data.repository

import androidx.paging.PagingSource
import io.github.drumber.kitsune.data.mapper.MediaMapper.toAnime
import io.github.drumber.kitsune.data.presentation.model.media.Anime
import io.github.drumber.kitsune.data.source.network.media.AnimeNetworkDataSource
import io.github.drumber.kitsune.data.source.network.media.AnimePagingDataSource
import io.github.drumber.kitsune.data.source.network.media.TrendingAnimePagingDataSource
import io.github.drumber.kitsune.data.source.network.media.model.NetworkAnime
import io.github.drumber.kitsune.domain_old.service.Filter

class AnimeRepository(
    private val animeNetworkDataSource: AnimeNetworkDataSource
) {

    suspend fun getAnime(id: String, filter: Filter): Anime? {
        return animeNetworkDataSource.getAnime(id, filter)?.toAnime()
    }

    suspend fun getLanguages(id: String): List<String> {
        return animeNetworkDataSource.getLanguages(id)
    }

    fun animePagingSource(filter: Filter): PagingSource<Int, NetworkAnime> {
        return AnimePagingDataSource(animeNetworkDataSource, filter)
    }

    fun trendingAnimePagingSource(filter: Filter): PagingSource<Int, NetworkAnime> {
        return TrendingAnimePagingDataSource(animeNetworkDataSource, filter)
    }
}