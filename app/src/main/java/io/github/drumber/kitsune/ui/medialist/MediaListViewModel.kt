package io.github.drumber.kitsune.ui.medialist

import androidx.paging.PagingData
import io.github.drumber.kitsune.constants.Kitsu
import io.github.drumber.kitsune.data.common.FilterOptions
import io.github.drumber.kitsune.data.common.media.MediaType
import io.github.drumber.kitsune.data.common.toFilter
import io.github.drumber.kitsune.data.presentation.model.media.Media
import io.github.drumber.kitsune.data.presentation.model.media.MediaSelector
import io.github.drumber.kitsune.data.presentation.model.media.RequestType
import io.github.drumber.kitsune.data.repository.AnimeRepository
import io.github.drumber.kitsune.data.repository.MangaRepository
import io.github.drumber.kitsune.ui.base.MediaCollectionViewModel
import kotlinx.coroutines.flow.Flow

class MediaListViewModel(
    private val animeRepository: AnimeRepository,
    private val mangaRepository: MangaRepository
) : MediaCollectionViewModel() {

    override fun getData(mediaSelector: MediaSelector): Flow<PagingData<Media>> {
        return when (mediaSelector.mediaType) {
            MediaType.Anime -> getAnimeData(mediaSelector.requestType, mediaSelector.filterOptions)
            MediaType.Manga -> getMangaData(mediaSelector.requestType, mediaSelector.filterOptions)
        } as Flow<PagingData<Media>>
    }

    private fun getAnimeData(type: RequestType, filterOptions: FilterOptions) = when (type) {
        RequestType.ALL -> animeRepository.animePager(
            filterOptions.toFilter(),
            Kitsu.DEFAULT_PAGE_SIZE
        )

        RequestType.TRENDING -> animeRepository.trendingAnimePager(
            filterOptions.toFilter(),
            Kitsu.DEFAULT_PAGE_SIZE
        )
    }

    private fun getMangaData(type: RequestType, filterOptions: FilterOptions) = when (type) {
        RequestType.ALL -> mangaRepository.mangaPager(
            filterOptions.toFilter(),
            Kitsu.DEFAULT_PAGE_SIZE
        )

        RequestType.TRENDING -> mangaRepository.trendingMangaPager(
            filterOptions.toFilter(),
            Kitsu.DEFAULT_PAGE_SIZE
        )
    }
}