package io.github.drumber.kitsune.ui.library.editentry

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.map
import androidx.lifecycle.viewModelScope
import io.github.drumber.kitsune.data.presentation.model.library.LibraryEntry
import io.github.drumber.kitsune.data.presentation.model.library.LibraryEntryModification
import io.github.drumber.kitsune.data.presentation.model.library.LibraryEntryWithModification
import io.github.drumber.kitsune.data.repository.LibraryRepository
import io.github.drumber.kitsune.domain.library.LibraryEntryUpdateFailureReason.NotFound
import io.github.drumber.kitsune.domain.library.LibraryEntryUpdateResult.Failure
import io.github.drumber.kitsune.domain.library.LibraryEntryUpdateResult.Success
import io.github.drumber.kitsune.domain.library.UpdateLibraryEntryUseCase
import io.github.drumber.kitsune.domain_old.service.Filter
import io.github.drumber.kitsune.util.logE
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class LibraryEditEntryViewModel(
    private val libraryRepository: LibraryRepository,
    private val updateLibraryEntryUseCase: UpdateLibraryEntryUseCase
) : ViewModel() {

    var uneditedLibraryEntryWrapper: LibraryEntryWithModification? = null
        private set

    private val _libraryEntryWithModification = MutableLiveData<LibraryEntryWithModification>()
    val libraryEntryWithModification
        get() = _libraryEntryWithModification as LiveData<LibraryEntryWithModification>

    private val _libraryEntry = MutableLiveData<LibraryEntry>()
    val libraryEntry
        get() = _libraryEntry as LiveData<LibraryEntry>

    private val _loadState = MutableLiveData(LoadState.NotLoading)
    val loadState
        get() = _loadState as LiveData<LoadState>

    val hasChanges: LiveData<Boolean> = libraryEntryWithModification.map {
        val uneditedWrapper = uneditedLibraryEntryWrapper ?: return@map false
        val entry = uneditedWrapper.libraryEntry.copy()
        // apply old modifications
        val oldModifiedEntry = uneditedWrapper.modification
            ?.applyToLibraryEntry(entry)
            ?: entry
        // apply new modifications
        val newModifiedEntry = it.modification
            ?.applyToLibraryEntry(entry)
            ?: entry
        oldModifiedEntry != newModifiedEntry
    }

    fun initLibraryEntry(libraryEntryId: String) {
        if (_libraryEntryWithModification.value != null) return

        _loadState.value = LoadState.Loading
        viewModelScope.launch(Dispatchers.IO) {
            val libraryEntry = getLibraryEntry(libraryEntryId) ?: run {
                _loadState.postValue(LoadState.CloseDialog)
                return@launch
            }
            _libraryEntry.postValue(libraryEntry)

            val libraryModification = libraryRepository.getLibraryEntryModification(libraryEntryId)
                    ?: LibraryEntryModification.withIdAndNulls(libraryEntryId)

            val libraryEntryWrapper = LibraryEntryWithModification(
                libraryEntry,
                libraryModification
            )
            uneditedLibraryEntryWrapper = libraryEntryWrapper.copy()
            _libraryEntryWithModification.postValue(libraryEntryWrapper)
        }.invokeOnCompletion {
            _loadState.postValue(LoadState.NotLoading)
        }
    }

    private suspend fun getLibraryEntry(libraryEntryId: String): LibraryEntry? {
        return try {
            libraryRepository.getLibraryEntryFromDatabase(libraryEntryId)
                ?: libraryRepository.fetchLibraryEntry(
                    libraryEntryId,
                    Filter().include("anime", "manga")
                )
        } catch (e: Exception) {
            logE("Failed to obtain library entry.", e)
            return null
        }
    }

    fun setLibraryModification(libraryModification: LibraryEntryModification) {
        libraryEntryWithModification.value
            ?.copy(modification = libraryModification)
            ?.let { updatedWrapper ->
                _libraryEntryWithModification.value = updatedWrapper
            }
    }

    fun updateLibraryEntry(block: (LibraryEntryModification) -> LibraryEntryModification) {
        val updatedLibraryModification = libraryEntryWithModification.value?.modification?.let(block)
        if (updatedLibraryModification != null) {
            setLibraryModification(updatedLibraryModification)
        }
    }

    fun saveChanges() {
        val libraryModification = libraryEntryWithModification.value?.modification ?: return

        _loadState.value = LoadState.Loading
        viewModelScope.launch(Dispatchers.IO) {
            val result = updateLibraryEntryUseCase.invoke(libraryModification)
            when {
                result is Success -> _loadState.postValue(LoadState.CloseDialog)
                result is Failure && result.reason is NotFound -> _loadState.postValue(LoadState.CloseDialog)
                result is Failure -> _loadState.postValue(LoadState.Error)
            }
        }
    }

    fun removeLibraryEntry() {
        val libraryEntryId = libraryEntry.value?.id ?: return
        _loadState.value = LoadState.Loading
        viewModelScope.launch(Dispatchers.IO) {
            try {
                libraryRepository.removeLibraryEntry(libraryEntryId)
                _loadState.postValue(LoadState.CloseDialog)
            } catch (e: Exception) {
                logE("Failed to remove library entry.", e)
                _loadState.postValue(LoadState.Error)
            }
        }
    }

    enum class LoadState {
        NotLoading,
        Loading,
        Error,
        CloseDialog
    }
}