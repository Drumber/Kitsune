package io.github.drumber.kitsune.domain_old.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import io.github.drumber.kitsune.constants.Repository
import io.github.drumber.kitsune.domain_old.model.infrastructure.media.unit.MediaUnit
import io.github.drumber.kitsune.domain_old.paging.ChaptersPagingDataSource
import io.github.drumber.kitsune.domain_old.paging.EpisodesPagingDataSource
import io.github.drumber.kitsune.domain_old.service.Filter
import io.github.drumber.kitsune.domain_old.service.anime.EpisodesService
import io.github.drumber.kitsune.domain_old.service.manga.ChaptersService
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