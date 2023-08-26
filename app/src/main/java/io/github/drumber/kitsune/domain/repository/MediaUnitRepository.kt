package io.github.drumber.kitsune.domain.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import io.github.drumber.kitsune.constants.Repository
import io.github.drumber.kitsune.domain.model.infrastructure.media.unit.MediaUnit
import io.github.drumber.kitsune.domain.paging.ChaptersPagingDataSource
import io.github.drumber.kitsune.domain.paging.EpisodesPagingDataSource
import io.github.drumber.kitsune.domain.service.Filter
import io.github.drumber.kitsune.domain.service.anime.EpisodesService
import io.github.drumber.kitsune.domain.service.manga.ChaptersService
import kotlinx.coroutines.flow.Flow

class MediaUnitRepository(
    private val episodesService: EpisodesService,
    private val chaptersService: ChaptersService
) {

    fun episodesCollection(pageSize: Int, filter: Filter, type: UnitType): Flow<PagingData<MediaUnit>> =
        Pager(
            config = PagingConfig(
                pageSize = pageSize,
                maxSize = Repository.MAX_CACHED_ITEMS
            ),
            pagingSourceFactory = {
                filter.pageLimit(pageSize)
                when (type) {
                    UnitType.Episode -> EpisodesPagingDataSource(episodesService, filter)
                    UnitType.Chapter -> ChaptersPagingDataSource(chaptersService, filter)
                }
            }
        ).flow as Flow<PagingData<MediaUnit>>

    enum class UnitType {
        Episode, Chapter
    }

}