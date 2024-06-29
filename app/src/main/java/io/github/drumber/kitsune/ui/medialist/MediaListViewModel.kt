package io.github.drumber.kitsune.ui.medialist

import androidx.paging.PagingData
import io.github.drumber.kitsune.constants.Kitsu
import io.github.drumber.kitsune.domain_old.model.MediaSelector
import io.github.drumber.kitsune.domain_old.model.MediaType
import io.github.drumber.kitsune.domain_old.model.infrastructure.media.BaseMedia
import io.github.drumber.kitsune.domain_old.repository.AnimeRepository
import io.github.drumber.kitsune.domain_old.repository.MangaRepository
import io.github.drumber.kitsune.ui.base.MediaCollectionViewModel
import kotlinx.coroutines.flow.Flow

class MediaListViewModel(
    private val animeRepository: AnimeRepository,
    private val mangaRepository: MangaRepository
) : MediaCollectionViewModel() {

    override fun getData(mediaSelector: MediaSelector): Flow<PagingData<BaseMedia>> {
        val filter = mediaSelector.filter
        val requestType = mediaSelector.requestType
        return when (mediaSelector.mediaType) {
            MediaType.Anime -> animeRepository.animeCollection(
                Kitsu.DEFAULT_PAGE_SIZE,
                filter,
                requestType
            ) as Flow<PagingData<BaseMedia>>

            MediaType.Manga -> mangaRepository.mangaCollection(
                Kitsu.DEFAULT_PAGE_SIZE,
                filter,
                requestType
            ) as Flow<PagingData<BaseMedia>>
        }
    }
}