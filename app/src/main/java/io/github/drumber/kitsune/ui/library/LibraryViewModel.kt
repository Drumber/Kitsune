package io.github.drumber.kitsune.ui.library

import androidx.lifecycle.*
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.github.jasminb.jsonapi.JSONAPIDocument
import io.github.drumber.kitsune.constants.Kitsu
import io.github.drumber.kitsune.data.model.library.LibraryEntry
import io.github.drumber.kitsune.data.repository.LibraryEntriesRepository
import io.github.drumber.kitsune.data.repository.UserRepository
import io.github.drumber.kitsune.data.room.LibraryEntryDao
import io.github.drumber.kitsune.data.service.Filter
import io.github.drumber.kitsune.data.service.library.LibraryEntriesService
import io.github.drumber.kitsune.exception.ReceivedDataException
import io.github.drumber.kitsune.util.logE
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class LibraryViewModel(
    val userRepository: UserRepository,
    private val libraryEntriesRepository: LibraryEntriesRepository,
    private val libraryEntriesService: LibraryEntriesService,
    private val libraryEntryDao: LibraryEntryDao
) : ViewModel() {

    val filter: LiveData<Filter?> = Transformations.map(userRepository.userLiveData) { user ->
        user?.id?.let { userId ->
            Filter()
                .filter("user_id", userId)
                .sort("status", "-progressed_at")
                .include("anime", "manga")
        }
    }

    val dataSource: Flow<PagingData<LibraryEntry>> = filter.asFlow().filterNotNull().flatMapLatest { filter ->
        libraryEntriesRepository.libraryEntries(Kitsu.DEFAULT_PAGE_SIZE_LIBRARY, filter)
    }.cachedIn(viewModelScope)

    var responseErrorListener: ((Throwable) -> Unit)? = null

    fun markEpisodeWatched(libraryEntry: LibraryEntry) {
        val newProgress = libraryEntry.progress?.plus(1)
        updateLibraryProgress(libraryEntry, newProgress)
    }

    fun markEpisodeUnwatched(libraryEntry: LibraryEntry) {
        if(libraryEntry.progress == 0) return
        val newProgress = libraryEntry.progress?.minus(1)
        updateLibraryProgress(libraryEntry, newProgress)
    }

    private fun updateLibraryProgress(oldEntry: LibraryEntry, newProgress: Int?) {
        val updatedEntry = LibraryEntry(
            id = oldEntry.id,
            progress = newProgress
        )

        viewModelScope.launch(Dispatchers.IO) {
            try {
                val response = libraryEntriesService.updateLibraryEntry(
                    updatedEntry.id,
                    JSONAPIDocument(updatedEntry)
                )

                response.get()?.let { libraryEntry ->
                    // update the database, but copy anime and manga object from old library first
                    libraryEntryDao.updateLibraryEntry(libraryEntry.copy(
                        anime = oldEntry.anime,
                        manga = oldEntry.manga
                    ))
                } ?: throw ReceivedDataException("Received data is 'null'.")
            } catch (e: Exception) {
                logE("Failed to update library entry progress.", e)
                withContext(Dispatchers.Main) {
                    responseErrorListener?.invoke(e)
                }
            }
        }
    }

    /** Set to the library entry which rating should be updated. */
    var lastRatedLibraryEntry: LibraryEntry? = null

    fun updateRating(rating: Int) {
        if (rating !in 2..20) {
            responseErrorListener?.invoke(IllegalArgumentException("Rating must be in range 2..20."))
            return
        }
        val user = userRepository.user ?: return
        val libraryEntry = lastRatedLibraryEntry ?: return

        val updatedEntry = LibraryEntry(
            id = libraryEntry.id,
            ratingTwenty = rating,
            anime = libraryEntry.anime,
            manga = libraryEntry.manga,
            user = user
        )

        viewModelScope.launch(Dispatchers.IO) {
            try {
                val response = libraryEntriesService.updateLibraryEntry(
                    libraryEntry.id,
                    JSONAPIDocument(updatedEntry)
                )

                response.get()?.let { newEntry ->
                    libraryEntryDao.updateLibraryEntry(newEntry.copy(
                        anime = libraryEntry.anime,
                        manga = libraryEntry.manga
                    ))
                }
            } catch (e: Exception) {
                logE("Failed to update rating.", e)
                withContext(Dispatchers.Main) {
                    responseErrorListener?.invoke(e)
                }
            }
        }
    }

}