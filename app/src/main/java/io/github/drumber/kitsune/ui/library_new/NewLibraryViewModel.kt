package io.github.drumber.kitsune.ui.library_new

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.cachedIn
import io.github.drumber.kitsune.data.common.model.library.LibraryFilterOptions
import io.github.drumber.kitsune.data.common.model.library.LibraryStatus
import io.github.drumber.kitsune.data.presentation.model.library.LibraryEntryWithModification
import io.github.drumber.kitsune.data.repository.library.LibraryPagingRepository
import io.github.drumber.kitsune.domain.library.UpdateLibraryEntryProgressUseCase
import io.github.drumber.kitsune.domain.library.UpdateLibraryEntryRatingUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class NewLibraryViewModel(
    libraryPagingRepository: LibraryPagingRepository,
    private val updateLibraryEntryProgress: UpdateLibraryEntryProgressUseCase,
    private val updateLibraryEntryRating: UpdateLibraryEntryRatingUseCase
) : ViewModel() {

    val currentLibraryEntriesPager = libraryPagingRepository.libraryEntriesWithNextMediaUnitPager(
        pageSize = 20,
        filter = LibraryFilterOptions(
            status = listOf(LibraryStatus.Current),
            sortBy = LibraryFilterOptions.SortBy.UPDATED_AT,
        )
    ).cachedIn(viewModelScope)

    fun incrementProgress(libraryEntryWithModification: LibraryEntryWithModification) {
        viewModelScope.launch(Dispatchers.IO) {
            val currentProgress = libraryEntryWithModification.progress ?: 0
            updateLibraryEntryProgress(
                libraryEntryWithModification.libraryEntry,
                currentProgress + 1
            )
        }
    }

    fun updateRating(libraryEntryId: String, ratingTwenty: Int?) {
        viewModelScope.launch(Dispatchers.IO) {
            updateLibraryEntryRating(libraryEntryId, ratingTwenty)
        }
    }
}