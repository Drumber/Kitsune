package io.github.drumber.kitsune.ui.library

import androidx.lifecycle.*
import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.paging.map
import io.github.drumber.kitsune.constants.Kitsu
import io.github.drumber.kitsune.data.manager.LibraryManager
import io.github.drumber.kitsune.data.manager.ResponseCallback
import io.github.drumber.kitsune.data.model.library.*
import io.github.drumber.kitsune.data.repository.LibraryEntriesRepository
import io.github.drumber.kitsune.data.repository.UserRepository
import io.github.drumber.kitsune.data.room.OfflineLibraryUpdateDao
import io.github.drumber.kitsune.data.service.Filter
import io.github.drumber.kitsune.preference.KitsunePref
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

class LibraryViewModel(
    val userRepository: UserRepository,
    private val libraryEntriesRepository: LibraryEntriesRepository,
    private val libraryManager: LibraryManager,
    val offlineLibraryUpdateDao: OfflineLibraryUpdateDao
) : ViewModel() {

    private val updateLock = Any()

    var responseListener: (ResponseCallback)? = null

    private val _filter = MutableLiveData(
        LibraryEntryFilter(
            KitsunePref.libraryEntryKind,
            KitsunePref.libraryEntryStatus,
        )
    )
    val filter get() = _filter as LiveData<LibraryEntryFilter>

    private val _searchQuery = MutableLiveData<String?>()
    val searchQuery get() = _searchQuery.value

    var scrollToTopAfterSearch = false

    private val isSyncingLibrary = MutableLiveData(false)

    private val isUpdatingLibraryProgress = MutableLiveData(false)

    private val isUpdatingLibraryRating = MutableLiveData(false)

    private val _isUpdatingOrSyncingLibrary = MediatorLiveData<Boolean>()
    val isUpdatingOrSyncingLibrary: LiveData<Boolean>
        get() = _isUpdatingOrSyncingLibrary

    init {
        // merge multiple live data states into single isUpdatingOrSyncingLibrary live data
        _isUpdatingOrSyncingLibrary.combine(
            isSyncingLibrary,
            isUpdatingLibraryProgress,
            isUpdatingLibraryRating
        )
    }

    /**
     * Combines multiple live data sources representing a boolean state into this mediator live data.
     * The combined state is the result of an OR operation on every given source live data.
     */
    private fun MediatorLiveData<Boolean>.combine(vararg sources: LiveData<Boolean>) {
        sources.forEach { source ->
            addSource(source) {
                synchronized(updateLock) {
                    this.value = sources
                        .map { it.value ?: false }
                        .reduce { state, element -> state || element }
                }
            }
        }
    }

    private val filterMediator = MediatorLiveData<LibraryEntryFilter?>().apply {
        addSource(userRepository.userLiveData) { this.value = buildLibraryEntryFilter() }
        addSource(filter) { this.value = buildLibraryEntryFilter() }
        addSource(_searchQuery) { this.value = buildLibraryEntryFilter() }
    }

    val dataSource: Flow<PagingData<LibraryEntryWrapper>> = filterMediator.asFlow().filterNotNull()
        .flatMapLatest { filter ->
            handleLibraryEntriesDataSource(filter)
                .map { pagingData ->
                    pagingData.map { entry ->
                        LibraryEntryWrapper(
                            entry,
                            offlineLibraryUpdateDao.getOfflineLibraryUpdate(entry.id)
                        )
                    }
                }
        }.cachedIn(viewModelScope)

    /**
     * If the search query is blank, request the remote mediator for data
     * otherwise search the library online.
     */
    private fun handleLibraryEntriesDataSource(filter: LibraryEntryFilter): Flow<PagingData<LibraryEntry>> {
        val searchQueryText = searchQuery
        return if (searchQueryText.isNullOrBlank()) {
            libraryEntriesRepository.libraryEntries(Kitsu.DEFAULT_PAGE_SIZE_LIBRARY, filter)
        } else {
            libraryEntriesRepository.searchLibraryEntries(
                Kitsu.DEFAULT_PAGE_SIZE_LIBRARY,
                filter.buildFilter()
                    .filter("title", searchQueryText)
            )
        }
    }

    private fun buildLibraryEntryFilter(): LibraryEntryFilter? {
        return userRepository.user?.id?.let { userId ->
            val requestFilter = Filter()
                .filter("user_id", userId)
                .sort("status", "-progressed_at")
                .include("anime", "manga")

            filter.value?.copy(initialFilter = requestFilter)
                ?: LibraryEntryFilter(
                    KitsunePref.libraryEntryKind,
                    KitsunePref.libraryEntryStatus,
                    requestFilter
                )
        }
    }

    fun searchLibrary(searchQueryText: String?) {
        if ((searchQuery ?: "").trim() != (searchQueryText ?: "").trim()) {
            scrollToTopAfterSearch = true
            _searchQuery.postValue(searchQueryText)
        }
    }

    fun invalidatePagingSource() {
        libraryEntriesRepository.invalidatePagingSources()
    }

    fun setLibraryEntryKind(kind: LibraryEntryKind) {
        KitsunePref.libraryEntryKind = kind
        _filter.value = LibraryEntryFilter(kind, KitsunePref.libraryEntryStatus)
    }

    fun setLibraryEntryStatus(status: List<Status>) {
        KitsunePref.libraryEntryStatus = status
        _filter.value = LibraryEntryFilter(KitsunePref.libraryEntryKind, status)
    }

    fun synchronizeOfflineLibraryUpdates() {
        isSyncingLibrary.value = true
        viewModelScope.launch(Dispatchers.IO) {
            libraryManager.synchronizeLibrary {
                responseListener?.invoke(it)
                isSyncingLibrary.postValue(false)
            }
        }
    }

    fun markEpisodeWatched(libraryEntryWrapper: LibraryEntryWrapper) {
        val newProgress = libraryEntryWrapper.progress?.plus(1)
        updateLibraryProgress(libraryEntryWrapper.libraryEntry, newProgress)
    }

    fun markEpisodeUnwatched(libraryEntryWrapper: LibraryEntryWrapper) {
        if (libraryEntryWrapper.progress == 0) return
        val newProgress = libraryEntryWrapper.progress?.minus(1)
        updateLibraryProgress(libraryEntryWrapper.libraryEntry, newProgress)
    }

    private fun updateLibraryProgress(oldEntry: LibraryEntry, newProgress: Int?) {
        isUpdatingLibraryProgress.value = true
        viewModelScope.launch(Dispatchers.IO) {
            libraryManager.updateProgress(oldEntry, newProgress) {
                responseListener?.invoke(it)
                isUpdatingLibraryProgress.postValue(false)
            }
        }
    }

    /** Set to the library entry which rating should be updated. */
    var lastRatedLibraryEntry: LibraryEntry? = null

    fun updateRating(rating: Int?) {
        val oldEntry = lastRatedLibraryEntry ?: return

        isUpdatingLibraryRating.value = true
        viewModelScope.launch(Dispatchers.IO) {
            libraryManager.updateRating(oldEntry, rating) {
                responseListener?.invoke(it)
                isUpdatingLibraryRating.postValue(false)
            }
        }
    }

}