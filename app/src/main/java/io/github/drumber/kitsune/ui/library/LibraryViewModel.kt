package io.github.drumber.kitsune.ui.library

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asFlow
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.paging.insertSeparators
import androidx.paging.map
import io.github.drumber.kitsune.constants.Kitsu
import io.github.drumber.kitsune.domain.database.LibraryEntryModificationDao
import io.github.drumber.kitsune.domain.manager.LibraryUpdateResponse
import io.github.drumber.kitsune.domain.manager.library.LibraryManager
import io.github.drumber.kitsune.domain.manager.library.SynchronizationResult
import io.github.drumber.kitsune.domain.mapper.toLibraryEntry
import io.github.drumber.kitsune.domain.mapper.toLibraryEntryModification
import io.github.drumber.kitsune.domain.mapper.toLocalLibraryEntry
import io.github.drumber.kitsune.domain.model.database.LocalLibraryEntryModification
import io.github.drumber.kitsune.domain.model.database.LocalLibraryModificationState.NOT_SYNCHRONIZED
import io.github.drumber.kitsune.domain.model.database.LocalLibraryModificationState.SYNCHRONIZING
import io.github.drumber.kitsune.domain.model.infrastructure.library.LibraryEntry
import io.github.drumber.kitsune.domain.model.common.library.LibraryStatus
import io.github.drumber.kitsune.domain.model.ui.library.LibraryEntryFilter
import io.github.drumber.kitsune.domain.model.ui.library.LibraryEntryKind
import io.github.drumber.kitsune.domain.model.ui.library.LibraryEntryUiModel
import io.github.drumber.kitsune.domain.model.ui.library.LibraryEntryWrapper
import io.github.drumber.kitsune.domain.repository.LibraryEntriesRepository
import io.github.drumber.kitsune.domain.repository.UserRepository
import io.github.drumber.kitsune.domain.service.Filter
import io.github.drumber.kitsune.exception.InvalidDataException
import io.github.drumber.kitsune.exception.NotFoundException
import io.github.drumber.kitsune.exception.SynchronizationException
import io.github.drumber.kitsune.preference.KitsunePref
import io.github.drumber.kitsune.util.logE
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class LibraryViewModel(
    val userRepository: UserRepository,
    private val libraryEntriesRepository: LibraryEntriesRepository,
    private val libraryManager: LibraryManager,
    private val libraryModificationDao: LibraryEntryModificationDao
) : ViewModel() {

    private val updateLock = Any()

    var responseListener: ((LibraryUpdateResponse) -> Unit)? = null
    var doRefreshListener: (() -> Unit)? = null

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

    val notSynchronizedLibraryEntryModifications =
        libraryModificationDao.getLibraryEntryModificationsWithStateLiveData(NOT_SYNCHRONIZED)

    val dataSource: Flow<PagingData<LibraryEntryUiModel>> = filterMediator.asFlow()
        .filterNotNull()
        .distinctUntilChanged()
        .flatMapLatest { filter ->
            handleLibraryEntriesDataSource(filter)
                .cachedIn(viewModelScope)
                .combine(
                    libraryModificationDao.getAllLibraryEntryModificationsLiveData().asFlow()
                ) { pagingData, modifications ->
                    pagingData.map { entry ->
                        val modification = modifications.find { it.id == entry.id }
                        LibraryEntryWrapper(
                            entry,
                            modification?.toLibraryEntryModification(),
                            modification?.state == SYNCHRONIZING
                        )
                    }
                }
                .map {
                    it.insertSeparators { before: LibraryEntryWrapper?, after: LibraryEntryWrapper? ->
                        // do not insert separators if currently searching
                        if (!searchQuery.isNullOrBlank()) return@insertSeparators null

                        when {
                            after?.status == null -> null
                            before == null || before.status != after.status ->
                                LibraryEntryUiModel.StatusSeparatorModel(after.status!!)

                            else -> null
                        }
                    }
                }.cachedIn(viewModelScope)
        }

    /**
     * If the search query is blank, request the remote mediator for data
     * otherwise search the library online.
     */
    private fun handleLibraryEntriesDataSource(filter: LibraryEntryFilter): Flow<PagingData<LibraryEntry>> {
        return if (filter.isFilteredBySearchQuery()) {
            libraryEntriesRepository.searchLibraryEntries(
                Kitsu.DEFAULT_PAGE_SIZE_LIBRARY,
                filter.buildFilter()
            )
        } else {
            libraryEntriesRepository.libraryEntries(Kitsu.DEFAULT_PAGE_SIZE_LIBRARY, filter)
                .map { pagingSource -> pagingSource.map { it.toLibraryEntry() } }
        }
    }

    private fun buildLibraryEntryFilter(): LibraryEntryFilter? {
        return userRepository.user?.id?.let { userId ->
            val requestFilter = Filter()
                .filter("user_id", userId)
                .sort("status", "-progressed_at")
                .include("anime", "manga")

            // if the search query is not blank, add it to the filter and we will later search for the given query
            val searchQueryText = searchQuery
            if (!searchQueryText.isNullOrBlank()) {
                requestFilter.filter("title", searchQueryText)
            }

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

    fun setLibraryEntryStatus(status: List<LibraryStatus>) {
        // clear status filter if all filters are selected
        val statusFilter = if (status.size == 5) emptyList() else status
        KitsunePref.libraryEntryStatus = statusFilter
        _filter.value = LibraryEntryFilter(KitsunePref.libraryEntryKind, statusFilter)
    }

    fun synchronizeOfflineLibraryUpdates() {
        isSyncingLibrary.value = true
        viewModelScope.launch(Dispatchers.IO) {
            val librarySyncResults = libraryManager.pushAllStoredLocalModificationsToService()
            val failedCount = librarySyncResults.count {
                it.value is SynchronizationResult.Failed
            }
            if (failedCount > 0) {
                responseListener?.invoke(
                    LibraryUpdateResponse.Error(
                        SynchronizationException("Failed to synchronize $failedCount library entries.")
                    )
                )
            } else {
                responseListener?.invoke(LibraryUpdateResponse.SyncedOnline)
            }
        }.invokeOnCompletion {
            isSyncingLibrary.postValue(false)
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
        val modification = LocalLibraryEntryModification.withIdAndNulls(
            libraryEntry.id ?: throw InvalidDataException("Library entry ID cannot be 'null'.")
        ).copy(progress = newProgress)

        isUpdatingLibraryProgress.value = true
        viewModelScope.launch(Dispatchers.IO) {
            try {
                updateLibraryEntry(modification)
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
        val libraryEntry = lastRatedLibraryEntry?.toLocalLibraryEntry() ?: return

        val updatedRating = rating ?: -1 // '-1' will be mapped to 'null' by the json serializer
        val modification = LocalLibraryEntryModification.withIdAndNulls(libraryEntry.id)
            .copy(ratingTwenty = updatedRating)

        isUpdatingLibraryRating.value = true
        viewModelScope.launch(Dispatchers.IO) {
            try {
                updateLibraryEntry(modification)
            } catch (e: Exception) {
                logE("Failed to update library entry rating.")
            } finally {
                isUpdatingLibraryRating.postValue(false)
            }
        }
    }

    private suspend fun updateLibraryEntry(modification: LocalLibraryEntryModification) {
        val updateResult = libraryManager.updateLibraryEntry(modification)

        // temp fix for issue #6
        if (updateResult is SynchronizationResult.Success && filterMediator.value?.isFilteredBySearchQuery() == true) {
            // trigger new search to show the updated data
            triggerAdapterUpdate()
        }

        withContext(Dispatchers.Main) {
            when (updateResult) {
                is SynchronizationResult.Success -> {
                    responseListener?.invoke(LibraryUpdateResponse.SyncedOnline)
                    scrollToUpdatedEntry(updateResult.libraryEntry.id)
                }

                is SynchronizationResult.Failed -> responseListener?.invoke(
                    LibraryUpdateResponse.Error(
                        updateResult.exception
                    )
                )

                is SynchronizationResult.NotFound -> responseListener?.invoke(
                    LibraryUpdateResponse.Error(
                        NotFoundException("Library entry not found.")
                    )
                )
            }
        }
    }

    private fun scrollToUpdatedEntry(libraryEntryId: String?) {
        scrollToUpdatedEntryId = libraryEntryId
    }

    /**
     * Signals that the recycler view was scrolled to the updated entry.
     */
    fun hasScrolledToUpdatedEntry() {
        scrollToUpdatedEntryId = null
    }

    fun triggerAdapterUpdate() {
        doRefreshListener?.invoke()
    }

}