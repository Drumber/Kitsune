package io.github.drumber.kitsune.ui.medialist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import io.github.drumber.kitsune.constants.Defaults
import io.github.drumber.kitsune.shared.Kitsu
import io.github.drumber.kitsune.data.common.Filter
import io.github.drumber.kitsune.data.common.FilterOptions
import io.github.drumber.kitsune.data.common.media.MediaType
import io.github.drumber.kitsune.data.common.toFilter
import io.github.drumber.kitsune.data.presentation.model.media.Media
import io.github.drumber.kitsune.data.presentation.model.media.MediaSelector
import io.github.drumber.kitsune.data.presentation.model.media.RequestType
import io.github.drumber.kitsune.data.presentation.model.media.identifier
import io.github.drumber.kitsune.data.repository.AnimeRepository
import io.github.drumber.kitsune.data.repository.MangaRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MediaListViewModel(
    private val animeRepository: AnimeRepository,
    private val mangaRepository: MangaRepository
) : ViewModel() {

    private val mediaSelectorFlow: StateFlow<MediaSelector?>

    val setMediaSelector: (MediaSelector) -> Unit

    init {
        val mutableMediaSelectorFlow = MutableSharedFlow<MediaSelector>(replay = 1)

        mediaSelectorFlow = mutableMediaSelectorFlow
            .distinctUntilChanged()
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.Lazily,
                initialValue = null
            )

        setMediaSelector = { mediaSelector ->
            viewModelScope.launch {
                mutableMediaSelectorFlow.emit(mediaSelector)
            }
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    val dataSource: Flow<PagingData<out Media>> = mediaSelectorFlow
        .filterNotNull()
        .distinctUntilChanged()
        .flatMapLatest { selector ->
            // copy the filter and limit the fields of the response model to only the required ones
            val mediaSelector = with(selector) {
                copy(
                    filterOptions = Filter(filterOptions.toMutableMap())
                        .fields(mediaType.identifier, *Defaults.MINIMUM_COLLECTION_FIELDS)
                        .options
                )
            }
            getData(mediaSelector)
        }.cachedIn(viewModelScope)

    private fun getData(mediaSelector: MediaSelector): Flow<PagingData<Media>> {
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