package io.github.drumber.kitsune.ui.details.episodes

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asFlow
import androidx.lifecycle.liveData
import androidx.lifecycle.switchMap
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import io.github.drumber.kitsune.constants.Kitsu
import io.github.drumber.kitsune.domain_old.database.LibraryEntryDao
import io.github.drumber.kitsune.domain_old.database.LibraryEntryModificationDao
import io.github.drumber.kitsune.domain_old.manager.library.LibraryManager
import io.github.drumber.kitsune.domain_old.manager.library.SynchronizationResult
import io.github.drumber.kitsune.domain_old.mapper.toLibraryEntry
import io.github.drumber.kitsune.domain_old.mapper.toLibraryEntryModification
import io.github.drumber.kitsune.domain_old.mapper.toLocalLibraryEntry
import io.github.drumber.kitsune.domain_old.model.database.LocalLibraryEntry
import io.github.drumber.kitsune.domain_old.model.database.LocalLibraryEntryModification
import io.github.drumber.kitsune.domain_old.model.database.LocalLibraryModificationState.SYNCHRONIZING
import io.github.drumber.kitsune.domain_old.model.infrastructure.media.Anime
import io.github.drumber.kitsune.domain_old.model.infrastructure.media.BaseMedia
import io.github.drumber.kitsune.domain_old.model.infrastructure.media.Manga
import io.github.drumber.kitsune.domain_old.model.infrastructure.media.unit.MediaUnit
import io.github.drumber.kitsune.domain_old.model.ui.library.LibraryEntryWrapper
import io.github.drumber.kitsune.domain_old.repository.MediaUnitRepository
import io.github.drumber.kitsune.domain_old.service.Filter
import io.github.drumber.kitsune.domain_old.service.library.LibraryEntriesService
import io.github.drumber.kitsune.exception.InvalidDataException
import io.github.drumber.kitsune.util.logE
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.launch

class EpisodesViewModel(
    private val mediaUnitRepository: MediaUnitRepository,
    libraryEntriesService: LibraryEntriesService,
    libraryEntryDao: LibraryEntryDao,
    private val libraryModificationDao: LibraryEntryModificationDao,
    private val libraryManager: LibraryManager
) : ViewModel() {

    private val acceptLibraryUpdateResult: (SynchronizationResult) -> Unit

    val libraryUpdateResultFlow: Flow<SynchronizationResult>

    private val media = MutableLiveData<BaseMedia>()

    private val libraryEntryId = MutableLiveData<String>()

    val libraryEntryWrapper = libraryEntryId.switchMap { id ->
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
                        libraryEntryDao.insertSingle(entry.toLocalLibraryEntry())
                        emitSource(libraryEntryDao.getLibraryEntryAsLiveData(id).mapToWrapper())
                    }
                } catch (e: Exception) {
                    logE("Failed to fetch library entry for id '$id'.", e)
                }
            }
        }
    }

    init {
        val mutableLibraryUpdateResultFlow = MutableSharedFlow<SynchronizationResult>()
        libraryUpdateResultFlow = mutableLibraryUpdateResultFlow.asSharedFlow()

        acceptLibraryUpdateResult = {
            viewModelScope.launch { mutableLibraryUpdateResultFlow.emit(it) }
        }
    }

    private fun LiveData<LocalLibraryEntry?>.mapToWrapper() = this.switchMap { libraryEntry ->
        liveData(Dispatchers.IO) {
            if (libraryEntry != null) {
                val modification = libraryModificationDao
                    .getLibraryEntryModification(libraryEntry.id)
                emit(
                    LibraryEntryWrapper(
                        libraryEntry.toLibraryEntry(),
                        modification?.toLibraryEntryModification(),
                        modification?.state == SYNCHRONIZING
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
        val libraryEntry = libraryEntryWrapper.value?.libraryEntry ?: return
        val number = mediaUnit.number ?: 0
        val progress = if (isWatched) {
            number
        } else {
            number.minus(1).coerceAtLeast(0)
        }

        val modification =
            LocalLibraryEntryModification.withIdAndNulls(
                libraryEntry.id ?: throw InvalidDataException("Library entry ID cannot be 'null'.")
            ).copy(progress = progress)

        viewModelScope.launch(Dispatchers.IO) {
            val updateResult = try {
                libraryManager.updateLibraryEntry(modification)
            } catch (e: Exception) {
                logE("Failed to update progress.", e)
                SynchronizationResult.Failed(e)
            }
            acceptLibraryUpdateResult(updateResult)
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