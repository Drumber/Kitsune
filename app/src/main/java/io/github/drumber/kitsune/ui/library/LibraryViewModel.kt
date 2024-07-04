package io.github.drumber.kitsune.ui.library

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.TerminalSeparatorType
import androidx.paging.cachedIn
import androidx.paging.insertSeparators
import androidx.paging.map
import io.github.drumber.kitsune.constants.Kitsu
import io.github.drumber.kitsune.data.presentation.model.library.LibraryEntry
import io.github.drumber.kitsune.data.presentation.model.library.LibraryEntryFilter
import io.github.drumber.kitsune.data.presentation.model.library.LibraryEntryKind
import io.github.drumber.kitsune.data.presentation.model.library.LibraryEntryWithModification
import io.github.drumber.kitsune.data.presentation.model.library.LibraryEntryWrapper
import io.github.drumber.kitsune.data.presentation.model.library.LibraryModificationState.NOT_SYNCHRONIZED
import io.github.drumber.kitsune.data.presentation.model.library.LibraryStatus
import io.github.drumber.kitsune.data.repository.LibraryRepository
import io.github.drumber.kitsune.data.repository.UserRepository
import io.github.drumber.kitsune.data.source.local.library.model.LocalLibraryEntryModification
import io.github.drumber.kitsune.domain.library.GetLibraryEntriesWithModificationsPagerUseCase
import io.github.drumber.kitsune.domain.library.SearchLibraryEntriesWithLocalModificationsPagerUseCase
import io.github.drumber.kitsune.domain.user.GetLocalUserIdUseCase
import io.github.drumber.kitsune.domain_old.manager.library.LibraryManager
import io.github.drumber.kitsune.domain_old.manager.library.SynchronizationResult
import io.github.drumber.kitsune.domain_old.model.ui.library.LibraryEntryUiModel
import io.github.drumber.kitsune.domain_old.service.Filter
import io.github.drumber.kitsune.exception.InvalidDataException
import io.github.drumber.kitsune.preference.KitsunePref
import io.github.drumber.kitsune.ui.library.InternalAction.LibraryUpdateOperationEnd
import io.github.drumber.kitsune.ui.library.InternalAction.LibraryUpdateOperationStart
import io.github.drumber.kitsune.ui.library.LibraryChangeResult.LibrarySynchronizationResult
import io.github.drumber.kitsune.ui.library.LibraryChangeResult.LibraryUpdateResult
import io.github.drumber.kitsune.util.formatUtcDate
import io.github.drumber.kitsune.util.getLocalCalendar
import io.github.drumber.kitsune.util.logE
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger

class LibraryViewModel(
    private val userRepository: UserRepository,
    private val getLocalUserId: GetLocalUserIdUseCase,
    private val libraryRepository: LibraryRepository,
    private val libraryManager: LibraryManager,
    private val getLibraryEntriesWithModifications: GetLibraryEntriesWithModificationsPagerUseCase,
    private val searchLibraryEntriesWithModification: SearchLibraryEntriesWithLocalModificationsPagerUseCase
) : ViewModel() {

    val state: StateFlow<UiState>

    val pagingDataFlow: Flow<PagingData<LibraryEntryUiModel>>

    val acceptAction: (UiAction) -> Unit

    private val acceptInternalAction: (InternalAction) -> Unit

    /**
     * The ID of the last updated entry the recycler view should scroll to.
     */
    var scrollToUpdatedEntryId: String? = null
        private set

    var doRefreshListener: (() -> Unit)? = null

    val libraryChangeResultFlow: Flow<LibraryChangeResult>

    val notSynchronizedLibraryEntryModifications =
        libraryRepository.getLibraryEntryModificationsByStateAsLiveData(NOT_SYNCHRONIZED)

    private val libraryProgressUpdateJobs = ConcurrentHashMap<String, Job>()

    val localUser = userRepository.localUser

    init {
        val initialFilter = FilterState(
            KitsunePref.libraryEntryKind,
            KitsunePref.libraryEntryStatus,
        )
        val actionStateFlow = MutableSharedFlow<UiAction>()
        val searches = actionStateFlow
            .filterIsInstance<UiAction.Filter>()
            .distinctUntilChanged()
            .onStart { emit(UiAction.Filter(filter = initialFilter)) }
        val queriesScrolled = actionStateFlow
            .filterIsInstance<UiAction.Scroll>()
            .distinctUntilChanged()
            .onStart { emit(UiAction.Scroll(initialFilter)) }

        val libraryUpdateOperationsCounter = AtomicInteger(0)
        val internalActionFlow = MutableSharedFlow<InternalAction>()
        val internalActionState = internalActionFlow
            .onEach { action ->
                when (action) {
                    is LibraryUpdateOperationStart -> libraryUpdateOperationsCounter.incrementAndGet()
                    is LibraryUpdateOperationEnd -> libraryUpdateOperationsCounter.decrementAndGet()
                    else -> {}
                }
            }
            .map {
                InternalState(
                    libraryOperationsCount = libraryUpdateOperationsCounter.get()
                )
            }
            .distinctUntilChanged()
            .onStart { emit(InternalState(libraryUpdateOperationsCounter.get())) }
        libraryChangeResultFlow = internalActionFlow
            .mapNotNull {
                when (it) {
                    is InternalAction.LibraryUpdateResult -> LibraryUpdateResult(it.result)
                    is InternalAction.LibrarySynchronizationResult -> LibrarySynchronizationResult(
                        it.results
                    )

                    else -> null
                }
            }

        pagingDataFlow = searches
            .mapNotNull { createLibraryEntryFilter(it.filter) }
            .flatMapLatest { filter ->
                getPagingLibraryEntriesFlow(filter)
                    .insertSeparators(filter)
            }
            .cachedIn(viewModelScope)

        state = combine(
            searches,
            queriesScrolled,
            internalActionState
        ) { search, scroll, internalState ->
            UiState(
                filter = search.filter,
                lastFilterScrolled = scroll.currentFilter,
                hasNotScrolledForCurrentFilter = search.filter != scroll.currentFilter,
                isLibraryUpdateOperationInProgress = internalState.libraryOperationsCount != 0
            )
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.Lazily,
            initialValue = UiState(initialFilter, initialFilter)
        )

        acceptAction = { action ->
            viewModelScope.launch { actionStateFlow.emit(action) }
        }

        acceptInternalAction = { action ->
            viewModelScope.launch { internalActionFlow.emit(action) }
        }
    }

    fun hasUser() = userRepository.hasLocalUser()

    private fun getPagingLibraryEntriesFlow(
        filter: LibraryEntryFilter
    ): Flow<PagingData<LibraryEntryWithModification>> {
        return if (filter.isFilteredBySearchQuery()) {
            // if filter contains a search query, then search directly using the paging source
            searchLibraryEntriesWithModification(Kitsu.DEFAULT_PAGE_SIZE_LIBRARY, filter.buildFilter())
        } else {
            // otherwise use the default paging source
            getLibraryEntriesWithModifications(Kitsu.DEFAULT_PAGE_SIZE_LIBRARY, filter)
        }
    }

    private fun Flow<PagingData<LibraryEntryWithModification>>.insertSeparators(
        filter: LibraryEntryFilter
    ): Flow<PagingData<LibraryEntryUiModel>> {
        return map { pagingData ->
            pagingData.map { LibraryEntryUiModel.EntryModel(it) }
        }.map {
            it.insertSeparators(TerminalSeparatorType.SOURCE_COMPLETE) { before, after ->
                when {
                    // do not insert separators if library is currently searched
                    filter.isFilteredBySearchQuery() -> null

                    after?.entry?.libraryEntry?.status == null -> null

                    before == null || before.entry.libraryEntry.status != after.entry.libraryEntry.status ->
                        LibraryEntryUiModel.StatusSeparatorModel(
                            status = after.entry.libraryEntry.status,
                            isMangaSelected = filter.kind == LibraryEntryKind.Manga
                        )

                    else -> null
                }
            }
        }
    }

    private fun createLibraryEntryFilter(filter: FilterState): LibraryEntryFilter? {
        return getLocalUserId()?.let { userId ->
            val requestFilter = Filter()
                .filter("user_id", userId)
                .sort("status", "-progressed_at")
                .include("anime", "manga")

            // if the search query is not blank, add it to the filter and we will search for the given query
            if (filter.searchQuery.isNotBlank()) {
                requestFilter.filter("title", filter.searchQuery)
            }

            LibraryEntryFilter(
                kind = filter.kind,
                libraryStatus = filter.libraryStatus,
                initialFilter = requestFilter
            )
        }
    }

    fun searchLibrary(searchQueryText: String) {
        val currentFilter = state.value.filter
        if (currentFilter.searchQuery.trim() != searchQueryText.trim()) {
            acceptAction(UiAction.Filter(currentFilter.copy(searchQuery = searchQueryText)))
        }
    }

    fun invalidatePagingSource() {
        libraryRepository.invalidatePagingSources()
    }

    fun setLibraryEntryKind(kind: LibraryEntryKind) {
        KitsunePref.libraryEntryKind = kind
        val currentFilter = state.value.filter
        acceptAction(UiAction.Filter(currentFilter.copy(kind = kind)))
    }

    fun setLibraryEntryStatus(status: List<LibraryStatus>) {
        // clear status filter if all filters are selected
        val statusFilter = if (status.size == 5) emptyList() else status
        KitsunePref.libraryEntryStatus = statusFilter
        val currentFilter = state.value.filter
        acceptAction(UiAction.Filter(currentFilter.copy(libraryStatus = statusFilter)))
    }

    fun synchronizeOfflineLibraryUpdates() {
        acceptInternalAction(LibraryUpdateOperationStart)
        viewModelScope.launch(Dispatchers.IO) {
            val librarySyncResults = libraryManager.pushAllStoredLocalModificationsToService()
            acceptInternalAction(InternalAction.LibrarySynchronizationResult(librarySyncResults.values.toList()))
        }.invokeOnCompletion {
            acceptInternalAction(LibraryUpdateOperationEnd)
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
        // set startedAt date when starting consuming library entry
        val startedAt = if (
            libraryEntry.startedAt.isNullOrBlank() &&
            newProgress == 1 &&
            (libraryEntry.progress ?: 0) == 0
        ) {
            getLocalCalendar().formatUtcDate()
        } else {
            null
        }

        val modification = LocalLibraryEntryModification.withIdAndNulls(
            libraryEntry.id ?: throw InvalidDataException("Library entry ID cannot be 'null'.")
        ).copy(progress = newProgress, startedAt = startedAt)

        val ongoingJob = libraryProgressUpdateJobs[modification.id]
        val job = viewModelScope.launch(Dispatchers.IO) {
            ongoingJob?.cancelAndJoin() // wait until ongoing update call is cancelled
            try {
                updateLibraryEntry(modification)
            } catch (e: CancellationException) {
                // request was cancelled by a subsequent update call
                return@launch
            } catch (e: Exception) {
                logE("Failed to update library entry progress.", e)
            }
        }
        libraryProgressUpdateJobs[modification.id] = job
        job.invokeOnCompletion {
            if (libraryProgressUpdateJobs[modification.id] == job) {
                libraryProgressUpdateJobs.remove(modification.id)
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

        viewModelScope.launch(Dispatchers.IO) {
            try {
                updateLibraryEntry(modification)
            } catch (e: Exception) {
                logE("Failed to update library entry rating.")
            }
        }
    }

    private suspend fun updateLibraryEntry(modification: LocalLibraryEntryModification) {
        acceptInternalAction(LibraryUpdateOperationStart)
        val updateResult = try {
            libraryManager.updateLibraryEntry(modification)
        } finally {
            acceptInternalAction(LibraryUpdateOperationEnd)
        }
        acceptInternalAction(InternalAction.LibraryUpdateResult(updateResult))

        if (updateResult is SynchronizationResult.Success) {
            scrollToUpdatedEntry(updateResult.libraryEntry.id)
        }

        if (updateResult is SynchronizationResult.Success && state.value.filter.searchQuery.isNotBlank()) {
            // trigger new search to show the updated data
            withContext(Dispatchers.Main) {
                triggerAdapterUpdate()
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

sealed class UiAction {
    data class Filter(val filter: FilterState) : UiAction()
    data class Scroll(val currentFilter: FilterState) : UiAction()
}

data class UiState(
    val filter: FilterState,
    val lastFilterScrolled: FilterState,
    val hasNotScrolledForCurrentFilter: Boolean = false,
    val isLibraryUpdateOperationInProgress: Boolean = false
)

data class FilterState(
    val kind: LibraryEntryKind = LibraryEntryKind.All,
    val libraryStatus: List<LibraryStatus> = emptyList(),
    val searchQuery: String = "",
)

sealed class LibraryChangeResult {
    data class LibraryUpdateResult(val result: SynchronizationResult) : LibraryChangeResult()
    data class LibrarySynchronizationResult(val results: List<SynchronizationResult>) :
        LibraryChangeResult()
}

private sealed class InternalAction {
    data object LibraryUpdateOperationStart : InternalAction()
    data object LibraryUpdateOperationEnd : InternalAction()
    data class LibraryUpdateResult(val result: SynchronizationResult) : InternalAction()
    data class LibrarySynchronizationResult(val results: List<SynchronizationResult>) :
        InternalAction()
}

private data class InternalState(
    val libraryOperationsCount: Int
)
