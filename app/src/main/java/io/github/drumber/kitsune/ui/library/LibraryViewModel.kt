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
import io.github.drumber.kitsune.util.ResponseData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class LibraryViewModel(
    userRepository: UserRepository,
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

    var episodeWatchProgressResponseListener: ((ResponseData<LibraryEntry>) -> Unit)? = null

    fun markEpisodeWatched(libraryEntry: LibraryEntry) {
        val updatedEntry = LibraryEntry(
            id = libraryEntry.id,
            progress = libraryEntry.progress?.plus(1)
        )
        updateLibraryProgress(updatedEntry, libraryEntry)
    }

    fun markEpisodeUnwatched(libraryEntry: LibraryEntry) {
        if(libraryEntry.progress == 0) return
        val updatedEntry = LibraryEntry(
            id = libraryEntry.id,
            progress = libraryEntry.progress?.minus(1)
        )
        updateLibraryProgress(updatedEntry, libraryEntry)
    }

    private fun updateLibraryProgress(updatedEntry: LibraryEntry, oldEntry: LibraryEntry) {
        viewModelScope.launch(Dispatchers.IO) {
            val responseData = try {
                val response = libraryEntriesService.updateLibraryEntry(updatedEntry.id, JSONAPIDocument(updatedEntry))

                response.get()?.let { libraryEntry ->
                    // update the database, but copy anime and manga object from old library first
                    libraryEntryDao.updateLibraryEntry(libraryEntry.copy(
                        anime = oldEntry.anime,
                        manga = oldEntry.manga
                    ))

                    ResponseData.Success(libraryEntry)
                } ?: throw ReceivedDataException("Received data is 'null'.")
            } catch (e: Exception) {
                ResponseData.Error(e)
            }

            withContext(Dispatchers.Main) {
                episodeWatchProgressResponseListener?.invoke(responseData)
            }
        }
    }

}