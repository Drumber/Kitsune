package io.github.drumber.kitsune.ui.details.episodes

import androidx.lifecycle.*
import androidx.paging.PagingData
import androidx.paging.cachedIn
import io.github.drumber.kitsune.constants.Kitsu
import io.github.drumber.kitsune.data.manager.LibraryManager
import io.github.drumber.kitsune.data.manager.ResponseCallback
import io.github.drumber.kitsune.data.model.library.LibraryEntry
import io.github.drumber.kitsune.data.model.library.LibraryEntryWrapper
import io.github.drumber.kitsune.data.model.media.Anime
import io.github.drumber.kitsune.data.model.media.BaseMedia
import io.github.drumber.kitsune.data.model.media.Manga
import io.github.drumber.kitsune.data.model.unit.MediaUnit
import io.github.drumber.kitsune.data.repository.MediaUnitRepository
import io.github.drumber.kitsune.data.room.LibraryEntryDao
import io.github.drumber.kitsune.data.room.OfflineLibraryUpdateDao
import io.github.drumber.kitsune.data.service.Filter
import io.github.drumber.kitsune.data.service.library.LibraryEntriesService
import io.github.drumber.kitsune.util.logE
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.launch

class EpisodesViewModel(
    private val mediaUnitRepository: MediaUnitRepository,
    libraryEntriesService: LibraryEntriesService,
    libraryEntryDao: LibraryEntryDao,
    private val offlineLibraryUpdateDao: OfflineLibraryUpdateDao,
    private val libraryManager: LibraryManager
) : ViewModel() {

    var responseListener: (ResponseCallback)? = null

    private val media = MutableLiveData<BaseMedia>()

    private val libraryEntryId = MutableLiveData<String>()

    val libraryEntryWrapper = Transformations.switchMap(libraryEntryId) { id ->
        val dbEntry = libraryEntryDao.getLibraryEntryAsLiveData(id)
        return@switchMap if (dbEntry.value != null) {
            // return cached library entry from database mapped to a library wrapper
            dbEntry.mapToWrapper()
        } else {
            // request library entry from server
            liveData(Dispatchers.IO) {
                try {
                    val entry = libraryEntriesService.getLibraryEntry(
                        id, Filter()
                            .include("anime", "manga")
                            .options
                    ).get()

                    if (entry != null) {
                        // add library entry to Room database
                        libraryEntryDao.insertSingle(entry)
                        emitSource(libraryEntryDao.getLibraryEntryAsLiveData(id).mapToWrapper())
                    }
                } catch (e: Exception) {
                    logE("Failed to fetch library entry for id '$id'.", e)
                }
            }
        }
    }

    private fun LiveData<LibraryEntry?>.mapToWrapper() = this.switchMap { libraryEntry ->
        liveData(Dispatchers.IO) {
            if (libraryEntry != null) {
                emit(
                    LibraryEntryWrapper(
                        libraryEntry,
                        offlineLibraryUpdateDao.getOfflineLibraryUpdate(libraryEntry.id)
                    )
                )
            } else {
                emit(null)
            }
        }
    }


    fun setMedia(media: BaseMedia) {
        if (media != this.media.value) {
            this.media.value = media
        }
    }

    fun setLibraryEntryId(id: String) {
        libraryEntryId.value = id
    }

    fun setMediaUnitWatched(mediaUnit: MediaUnit, isWatched: Boolean) {
        val oldLibraryEntry = libraryEntryWrapper.value?.libraryEntry ?: return
        val number = mediaUnit.number ?: 0
        val progress = if (isWatched) {
            number
        } else {
            number.minus(1).coerceAtLeast(0)
        }

        viewModelScope.launch(Dispatchers.IO) {
            libraryManager.updateProgress(oldLibraryEntry, progress) {
                responseListener?.invoke(it)
            }
        }
    }

    val dataSource: Flow<PagingData<MediaUnit>> = media.asFlow().flatMapLatest { media ->
        val filter = Filter()
            .sort("number")
        val type = when (media) {
            is Anime -> {
                filter.filter("media_id", media.id)
                MediaUnitRepository.UnitType.Episode
            }
            is Manga -> {
                filter.filter("manga_id", media.id)
                MediaUnitRepository.UnitType.Chapter
            }
        }
        mediaUnitRepository.episodesCollection(Kitsu.DEFAULT_PAGE_SIZE, filter, type)
    }.cachedIn(viewModelScope)

}