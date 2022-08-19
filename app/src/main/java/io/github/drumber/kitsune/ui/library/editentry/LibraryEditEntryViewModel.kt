package io.github.drumber.kitsune.ui.library.editentry

import androidx.lifecycle.*
import io.github.drumber.kitsune.data.manager.LibraryManager
import io.github.drumber.kitsune.data.manager.LibraryUpdateResponse
import io.github.drumber.kitsune.data.model.library.LibraryEntry
import io.github.drumber.kitsune.data.model.library.LibraryEntryWrapper
import io.github.drumber.kitsune.data.model.library.LibraryModification
import io.github.drumber.kitsune.data.room.LibraryEntryDao
import io.github.drumber.kitsune.data.room.OfflineLibraryModificationDao
import io.github.drumber.kitsune.data.service.Filter
import io.github.drumber.kitsune.data.service.library.LibraryEntriesService
import io.github.drumber.kitsune.util.logE
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class LibraryEditEntryViewModel(
    private val libraryManager: LibraryManager,
    private val libraryEntryDao: LibraryEntryDao,
    private val libraryModificationDao: OfflineLibraryModificationDao,
    private val libraryEntriesService: LibraryEntriesService
) : ViewModel() {

    var uneditedLibraryEntryWrapper: LibraryEntryWrapper? = null
        private set

    private val _libraryEntryWrapper = MutableLiveData<LibraryEntryWrapper>()
    val libraryEntryWrapper
        get() = _libraryEntryWrapper as LiveData<LibraryEntryWrapper>

    private val _libraryEntry = MutableLiveData<LibraryEntry>()
    val libraryEntry
        get() = _libraryEntry as LiveData<LibraryEntry>

    private val _loadState = MutableLiveData(LoadState.NotLoading)
    val loadState
        get() = _loadState as LiveData<LoadState>

    val hasChanges: LiveData<Boolean> = libraryEntryWrapper.map {
        val uneditedWrapper = uneditedLibraryEntryWrapper ?: return@map false
        val entry = uneditedWrapper.libraryEntry.copy()
        // apply old modifications
        val oldModifiedEntry = uneditedWrapper.libraryModification?.applyToLibraryEntry(entry.copy()) ?: entry
        // apply new modifications
        val newModifiedEntry = it.libraryModification?.applyToLibraryEntry(entry.copy()) ?: entry
        oldModifiedEntry != newModifiedEntry
    }

    fun initLibraryEntry(libraryEntryId: String) {
        if (_libraryEntryWrapper.value != null) return

        _loadState.value = LoadState.Loading
        viewModelScope.launch(Dispatchers.IO) {
            val libraryEntry = getLibraryEntry(libraryEntryId) ?: run {
                _loadState.postValue(LoadState.CloseDialog)
                return@launch
            }
            _libraryEntry.postValue(libraryEntry)

            val libraryModification =
                libraryModificationDao.getOfflineLibraryModification(libraryEntryId)
                    ?: LibraryModification(libraryEntryId)

            val libraryEntryWrapper = LibraryEntryWrapper(libraryEntry, libraryModification)
            uneditedLibraryEntryWrapper = libraryEntryWrapper.copy()
            _libraryEntryWrapper.postValue(libraryEntryWrapper)
        }.invokeOnCompletion {
            _loadState.postValue(LoadState.NotLoading)
        }
    }

    private suspend fun getLibraryEntry(libraryEntryId: String): LibraryEntry? {
        return try {
            libraryEntryDao.getLibraryEntry(libraryEntryId)
                ?: libraryEntriesService.getLibraryEntry(
                    libraryEntryId,
                    Filter().include("anime", "manga").options
                ).get()
        } catch (e: Exception) {
            logE("Failed to obtain library entry.", e)
            return null
        }
    }

    fun setLibraryModification(libraryModification: LibraryModification) {
        libraryEntryWrapper.value
            ?.copy(libraryModification = libraryModification)
            ?.let { updatedWrapper ->
                _libraryEntryWrapper.value = updatedWrapper
            }
    }

    fun updateLibraryEntry(block: (LibraryModification) -> LibraryModification) {
        val updatedLibraryModification = libraryEntryWrapper.value?.libraryModification?.let(block)
        if (updatedLibraryModification != null) {
            setLibraryModification(updatedLibraryModification)
        }
    }

    fun saveChanges() {
        val libraryModification = libraryEntryWrapper.value?.libraryModification ?: return

        _loadState.value = LoadState.Loading
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val response = libraryManager.updateLibraryEntry(libraryModification)
                if (response !is LibraryUpdateResponse.Error) {
                    _loadState.postValue(LoadState.CloseDialog)
                } else {
                    throw response.exception
                }
            } catch (e: Exception) {
                logE("Failed to update library entry.", e)
                _loadState.postValue(LoadState.Error)
            }
        }
    }

    fun removeLibraryEntry() {
        val libraryEntry = libraryEntry.value ?: return
        _loadState.value = LoadState.Loading
        viewModelScope.launch(Dispatchers.IO) {
            try {
                libraryManager.removeLibraryEntry(libraryEntry)
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