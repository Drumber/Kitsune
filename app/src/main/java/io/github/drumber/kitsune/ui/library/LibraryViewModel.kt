package io.github.drumber.kitsune.ui.library

import androidx.lifecycle.*
import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.paging.map
import io.github.drumber.kitsune.constants.Kitsu
import io.github.drumber.kitsune.data.manager.LibraryManager
import io.github.drumber.kitsune.data.manager.LibraryUpdateResponse
import io.github.drumber.kitsune.data.model.library.*
import io.github.drumber.kitsune.data.repository.LibraryEntriesRepository
import io.github.drumber.kitsune.data.repository.UserRepository
import io.github.drumber.kitsune.data.room.OfflineLibraryModificationDao
import io.github.drumber.kitsune.data.service.Filter
import io.github.drumber.kitsune.preference.KitsunePref
import io.github.drumber.kitsune.util.logE
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class LibraryViewModel(
    val userRepository: UserRepository,
    private val libraryEntriesRepository: LibraryEntriesRepository,
    private val libraryManager: LibraryManager,
    val offlineLibraryModificationDao: OfflineLibraryModificationDao
) : ViewModel() {

    private val updateLock = Any()

    var responseListener: ((LibraryUpdateResponse) -> Unit)? = null

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

    /**
     * The ID of the last updated entry the fragment should scroll to.
     */
    var scrollToUpdatedEntryId: String? = null
        private set

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
                            offlineLibraryModificationDao.getOfflineLibraryModification(entry.id)
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
        // clear status filter if all filters are selected
        val statusFilter = if (status.size == 5) emptyList() else status
        KitsunePref.libraryEntryStatus = statusFilter
        _filter.value = LibraryEntryFilter(KitsunePref.libraryEntryKind, statusFilter)
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

    private fun updateLibraryProgress(libraryEntry: LibraryEntry, newProgress: Int?) {
        val modification = LibraryModification(libraryEntry.id, progress = newProgress)

        isUpdatingLibraryProgress.value = true
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val response = libraryManager.updateLibraryEntry(modification)
                withContext(Dispatchers.Main) {
                    responseListener?.invoke(response)
                    scrollToUpdatedEntry(response, libraryEntry.id)
                }
            } catch (e: Exception) {
                logE("Failed to update library entry progress.", e)
            } finally {
                isUpdatingLibraryProgress.postValue(false)
            }
        }
    }

    /** Set to the library entry which rating should be updated. */
    var lastRatedLibraryEntry: LibraryEntry? = null

    fun updateRating(rating: Int?) {
        val libraryEntry = lastRatedLibraryEntry ?: return

        val updatedRating = rating ?: -1 // '-1' will be mapped to 'null' by the json serializer
        val modification = LibraryModification(libraryEntry.id, ratingTwenty = updatedRating)

        isUpdatingLibraryRating.value = true
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val response = libraryManager.updateLibraryEntry(modification)
                withContext(Dispatchers.Main) {
                    responseListener?.invoke(response)
                    scrollToUpdatedEntry(response, libraryEntry.id)
                }
            } catch (e: Exception) {
                logE("Failed to update library entry rating.")
            } finally {
                isUpdatingLibraryRating.postValue(false)
            }
        }
    }

    private fun scrollToUpdatedEntry(response: LibraryUpdateResponse, libraryEntryId: String?) {
        if (response is LibraryUpdateResponse.SyncedOnline) {
            scrollToUpdatedEntryId = libraryEntryId
        }
    }

    /**
     * Signals that the recycler view was scrolled to the updated entry.
     */
    fun hasScrolledToUpdatedEntry() {
        scrollToUpdatedEntryId = null
    }

}