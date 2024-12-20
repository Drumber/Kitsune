package io.github.drumber.kitsune.ui.library_new

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.cachedIn
import io.github.drumber.kitsune.data.common.library.LibraryFilterOptions
import io.github.drumber.kitsune.data.presentation.model.library.LibraryEntryWithModificationAndNextUnit
import io.github.drumber.kitsune.data.presentation.model.library.LibraryStatus
import io.github.drumber.kitsune.data.repository.library.LibraryRepository
import io.github.drumber.kitsune.domain.auth.IsUserLoggedInUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class NewLibraryViewModel(
    isUserLoggedIn: IsUserLoggedInUseCase,
    libraryRepository: LibraryRepository
) : ViewModel() {

    private val _currentLibraryEntries =
        MutableStateFlow(emptyList<LibraryEntryWithModificationAndNextUnit>())
    val currentLibraryEntries = _currentLibraryEntries.asStateFlow()

    val currentLibraryEntriesPager = libraryRepository.libraryEntriesWithNextMediaUnitPager(
        pageSize = 20,
        filter = LibraryFilterOptions(
            status = listOf(LibraryStatus.Current),
            sortBy = LibraryFilterOptions.SortBy.UPDATED_AT,
        )
    ).cachedIn(viewModelScope)

    init {
//        if (isUserLoggedIn()) {
//            viewModelScope.launch {
//                try {
//                    val libraryEntries = libraryRepository.getCurrentLibraryEntriesWithNextUnit()
//                        ?: throw NoDataException()
//                    _currentLibraryEntries.value = libraryEntries
//                } catch (e: Exception) {
//                    logE("Failed to get library entries", e)
//                }
//            }
//        }
    }
}