package io.github.drumber.kitsune.data.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.map
import io.github.drumber.kitsune.constants.Repository
import io.github.drumber.kitsune.data.mapper.MediaUnitMapper.toMediaUnit
import io.github.drumber.kitsune.data.source.network.media.ChapterNetworkDataSource
import io.github.drumber.kitsune.data.source.network.media.ChapterPagingDataSource
import io.github.drumber.kitsune.data.source.network.media.EpisodeNetworkDataSource
import io.github.drumber.kitsune.data.source.network.media.EpisodePagingDataSource
import io.github.drumber.kitsune.domain_old.service.Filter
import kotlinx.coroutines.flow.map

class MediaUnitRepository(
    private val episodeNetworkDataSource: EpisodeNetworkDataSource,
    private val chapterNetworkDataSource: ChapterNetworkDataSource
) {

    fun mediaUnitPager(type: MediaUnitType, filter: Filter, pageSize: Int) = Pager(
        config = PagingConfig(
            pageSize = pageSize,
            maxSize = Repository.MAX_CACHED_ITEMS
        ),
        pagingSourceFactory = {
            when (type) {
                MediaUnitType.EPISODE -> EpisodePagingDataSource(episodeNetworkDataSource, filter.pageLimit(pageSize))
                MediaUnitType.CHAPTER -> ChapterPagingDataSource(chapterNetworkDataSource, filter.pageLimit(pageSize))
            }
        }
    ).flow.map { pagingData ->
        pagingData.map { it.toMediaUnit() }
    }

    enum class MediaUnitType {
        EPISODE,
        CHAPTER
    }
}