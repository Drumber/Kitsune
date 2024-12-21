package io.github.drumber.kitsune.ui.library_new

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.cachedIn
import io.github.drumber.kitsune.data.common.library.LibraryFilterOptions
import io.github.drumber.kitsune.data.presentation.model.library.LibraryStatus
import io.github.drumber.kitsune.data.repository.library.LibraryPagingRepository

class NewLibraryViewModel(
    libraryPagingRepository: LibraryPagingRepository
) : ViewModel() {

    val currentLibraryEntriesPager = libraryPagingRepository.libraryEntriesWithNextMediaUnitPager(
        pageSize = 20,
        filter = LibraryFilterOptions(
            status = listOf(LibraryStatus.Current),
            sortBy = LibraryFilterOptions.SortBy.UPDATED_AT,
        )
    ).cachedIn(viewModelScope)
}