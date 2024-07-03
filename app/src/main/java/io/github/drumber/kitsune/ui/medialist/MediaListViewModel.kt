package io.github.drumber.kitsune.ui.medialist

import androidx.paging.PagingData
import io.github.drumber.kitsune.constants.Kitsu
import io.github.drumber.kitsune.data.presentation.model.media.Media
import io.github.drumber.kitsune.data.presentation.model.media.MediaSelector
import io.github.drumber.kitsune.data.presentation.model.media.MediaType
import io.github.drumber.kitsune.data.presentation.model.media.RequestType
import io.github.drumber.kitsune.data.repository.AnimeRepository
import io.github.drumber.kitsune.data.repository.MangaRepository
import io.github.drumber.kitsune.domain_old.service.Filter
import io.github.drumber.kitsune.ui.base.MediaCollectionViewModel
import kotlinx.coroutines.flow.Flow

class MediaListViewModel(
    private val animeRepository: AnimeRepository,
    private val mangaRepository: MangaRepository
) : MediaCollectionViewModel() {

    override fun getData(mediaSelector: MediaSelector): Flow<PagingData<Media>> {
        return when (mediaSelector.mediaType) {
            MediaType.Anime -> getAnimeData(mediaSelector.requestType, mediaSelector.filter)
            MediaType.Manga -> getMangaData(mediaSelector.requestType, mediaSelector.filter)
        } as Flow<PagingData<Media>>
    }

    private fun getAnimeData(type: RequestType, filter: Filter) = when (type) {
        RequestType.ALL -> animeRepository.animePager(
            filter,
            Kitsu.DEFAULT_PAGE_SIZE
        )

        RequestType.TRENDING -> animeRepository.trendingAnimePager(
            filter,
            Kitsu.DEFAULT_PAGE_SIZE
        )
    }

    private fun getMangaData(type: RequestType, filter: Filter) = when (type) {
        RequestType.ALL -> mangaRepository.mangaPager(
            filter,
            Kitsu.DEFAULT_PAGE_SIZE
        )

        RequestType.TRENDING -> mangaRepository.trendingMangaPager(
            filter,
            Kitsu.DEFAULT_PAGE_SIZE
        )
    }
}