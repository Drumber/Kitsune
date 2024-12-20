package io.github.drumber.kitsune.ui.library_new

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.drumber.kitsune.data.common.exception.NoDataException
import io.github.drumber.kitsune.data.presentation.model.library.LibraryEntryWithModificationAndNextUnit
import io.github.drumber.kitsune.data.repository.library.LibraryRepository
import io.github.drumber.kitsune.domain.auth.IsUserLoggedInUseCase
import io.github.drumber.kitsune.util.logE
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class NewLibraryViewModel(
    isUserLoggedIn: IsUserLoggedInUseCase,
    libraryRepository: LibraryRepository
) : ViewModel() {

    private val _currentLibraryEntries =
        MutableStateFlow(emptyList<LibraryEntryWithModificationAndNextUnit>())
    val currentLibraryEntries = _currentLibraryEntries.asStateFlow()

    init {
        if (isUserLoggedIn()) {
            viewModelScope.launch {
                try {
                    val libraryEntries = libraryRepository.getCurrentLibraryEntriesWithNextUnit()
                        ?: throw NoDataException()
                    _currentLibraryEntries.value = libraryEntries
                } catch (e: Exception) {
                    logE("Failed to get library entries", e)
                }
            }
        }
    }
}