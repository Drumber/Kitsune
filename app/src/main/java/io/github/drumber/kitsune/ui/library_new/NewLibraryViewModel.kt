package io.github.drumber.kitsune.ui.library_new

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.cachedIn
import io.github.drumber.kitsune.data.common.library.LibraryFilterOptions
import io.github.drumber.kitsune.data.presentation.model.library.LibraryEntryWithModification
import io.github.drumber.kitsune.data.presentation.model.library.LibraryStatus
import io.github.drumber.kitsune.data.repository.library.LibraryPagingRepository
import io.github.drumber.kitsune.domain.library.UpdateLibraryEntryProgressUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class NewLibraryViewModel(
    libraryPagingRepository: LibraryPagingRepository,
    private val updateLibraryEntryProgress: UpdateLibraryEntryProgressUseCase
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
}