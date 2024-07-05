package io.github.drumber.kitsune.ui.details.episodes

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asFlow
import androidx.lifecycle.switchMap
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import io.github.drumber.kitsune.constants.Kitsu
import io.github.drumber.kitsune.data.presentation.model.media.Anime
import io.github.drumber.kitsune.data.presentation.model.media.Manga
import io.github.drumber.kitsune.data.presentation.model.media.Media
import io.github.drumber.kitsune.data.presentation.model.media.unit.MediaUnit
import io.github.drumber.kitsune.data.repository.LibraryRepository
import io.github.drumber.kitsune.data.repository.MediaUnitRepository
import io.github.drumber.kitsune.data.repository.MediaUnitRepository.MediaUnitType
import io.github.drumber.kitsune.domain.library.LibraryEntryUpdateResult
import io.github.drumber.kitsune.domain.library.UpdateLibraryEntryProgressUseCase
import io.github.drumber.kitsune.domain_old.service.Filter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.launch

class EpisodesViewModel(
    private val mediaUnitRepository: MediaUnitRepository,
    private val libraryRepository: LibraryRepository,
    private val updateLibraryEntryProgress: UpdateLibraryEntryProgressUseCase
) : ViewModel() {

    private val acceptLibraryUpdateResult: (LibraryEntryUpdateResult) -> Unit

    val libraryUpdateResultFlow: Flow<LibraryEntryUpdateResult>

    private val media = MutableLiveData<Media>()

    val libraryEntryWrapper = media.switchMap { media ->
        val dbEntry = libraryRepository.getLibraryEntryWithModificationFromMediaAsLiveData(media.id)
        if (dbEntry.value == null) {
            viewModelScope.launch(Dispatchers.IO) {
                libraryRepository.fetchAndStoreLibraryEntryForMedia(media)
            }
        }
        return@switchMap dbEntry
    }

    init {
        val mutableLibraryUpdateResultFlow = MutableSharedFlow<LibraryEntryUpdateResult>()
        libraryUpdateResultFlow = mutableLibraryUpdateResultFlow.asSharedFlow()

        acceptLibraryUpdateResult = {
            viewModelScope.launch { mutableLibraryUpdateResultFlow.emit(it) }
        }
    }

    fun setMedia(media: Media) {
        if (media != this.media.value) {
            this.media.value = media
        }
    }

    fun setMediaUnitWatched(mediaUnit: MediaUnit, isWatched: Boolean) {
        val libraryEntry = libraryEntryWrapper.value?.libraryEntry ?: return
        val number = mediaUnit.number ?: 0
        val progress = if (isWatched) {
            number
        } else {
            number.minus(1).coerceAtLeast(0)
        }

        viewModelScope.launch(Dispatchers.IO) {
            val updateResult = updateLibraryEntryProgress(libraryEntry, progress)
            acceptLibraryUpdateResult(updateResult)
        }
    }

    val dataSource: Flow<PagingData<MediaUnit>> = media.asFlow().flatMapLatest { media ->
        val filter = Filter()
            .sort("number")
        val type = when (media) {
            is Anime -> {
                filter.filter("media_id", media.id)
                MediaUnitType.EPISODE
            }

            is Manga -> {
                filter.filter("manga_id", media.id)
                MediaUnitType.CHAPTER
            }
        }
        mediaUnitRepository.mediaUnitPager(type, filter, Kitsu.DEFAULT_PAGE_SIZE)
    }.cachedIn(viewModelScope)

}