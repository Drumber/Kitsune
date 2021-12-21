package io.github.drumber.kitsune.ui.library

import androidx.lifecycle.*
import androidx.paging.PagingData
import androidx.paging.cachedIn
import io.github.drumber.kitsune.constants.Kitsu
import io.github.drumber.kitsune.data.manager.LibraryManager
import io.github.drumber.kitsune.data.manager.ResponseCallback
import io.github.drumber.kitsune.data.model.library.LibraryEntry
import io.github.drumber.kitsune.data.model.library.LibraryEntryFilter
import io.github.drumber.kitsune.data.model.library.LibraryEntryKind
import io.github.drumber.kitsune.data.model.library.Status
import io.github.drumber.kitsune.data.repository.LibraryEntriesRepository
import io.github.drumber.kitsune.data.repository.UserRepository
import io.github.drumber.kitsune.data.service.Filter
import io.github.drumber.kitsune.preference.KitsunePref
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.launch

class LibraryViewModel(
    val userRepository: UserRepository,
    private val libraryEntriesRepository: LibraryEntriesRepository,
    private val libraryManager: LibraryManager
) : ViewModel() {

    val filter = MutableLiveData(
        LibraryEntryFilter(
            KitsunePref.libraryEntryKind,
            KitsunePref.libraryEntryStatus,
        )
    )

    private val filterMediator = MediatorLiveData<LibraryEntryFilter?>().apply {
        addSource(userRepository.userLiveData) { this.value = buildLibraryEntryFilter() }
        addSource(filter) { this.value = buildLibraryEntryFilter() }
    }

    val dataSource: Flow<PagingData<LibraryEntry>> = filterMediator.asFlow().filterNotNull()
        .flatMapLatest { filter ->
            libraryEntriesRepository.libraryEntries(Kitsu.DEFAULT_PAGE_SIZE_LIBRARY, filter)
        }.cachedIn(viewModelScope)

    private fun buildLibraryEntryFilter(): LibraryEntryFilter? {
        return userRepository.user?.id?.let { userId ->
            val reqFilter = Filter()
                .filter("user_id", userId)
                .sort("status", "-progressed_at")
                .include("anime", "manga")

            filter.value?.copy(initialFilter = reqFilter)
                ?: LibraryEntryFilter(
                    KitsunePref.libraryEntryKind,
                    KitsunePref.libraryEntryStatus,
                    reqFilter
                )
        }
    }

    fun setLibraryEntryKind(kind: LibraryEntryKind) {
        KitsunePref.libraryEntryKind = kind
        filter.value = LibraryEntryFilter(kind, KitsunePref.libraryEntryStatus)
    }

    fun setLibraryEntryStatus(status: List<Status>) {
        KitsunePref.libraryEntryStatus = status
        filter.value = LibraryEntryFilter(KitsunePref.libraryEntryKind, status)
    }

    var responseListener: (ResponseCallback)? = null

    fun markEpisodeWatched(libraryEntry: LibraryEntry) {
        val newProgress = libraryEntry.progress?.plus(1)
        updateLibraryProgress(libraryEntry, newProgress)
    }

    fun markEpisodeUnwatched(libraryEntry: LibraryEntry) {
        if (libraryEntry.progress == 0) return
        val newProgress = libraryEntry.progress?.minus(1)
        updateLibraryProgress(libraryEntry, newProgress)
    }

    private fun updateLibraryProgress(oldEntry: LibraryEntry, newProgress: Int?) {
        viewModelScope.launch(Dispatchers.IO) {
            libraryManager.updateProgress(oldEntry, newProgress) {
                responseListener?.invoke(it)
            }
        }
    }

    /** Set to the library entry which rating should be updated. */
    var lastRatedLibraryEntry: LibraryEntry? = null

    fun updateRating(rating: Int?) {
        val oldEntry = lastRatedLibraryEntry ?: return

        viewModelScope.launch(Dispatchers.IO) {
            libraryManager.updateRating(oldEntry, rating) {
                responseListener?.invoke(it)
            }
        }
    }

}