package io.github.drumber.kitsune.ui.library.editentry

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.map
import androidx.lifecycle.viewModelScope
import io.github.drumber.kitsune.domain.database.LibraryEntryDao
import io.github.drumber.kitsune.domain.database.LibraryEntryModificationDao
import io.github.drumber.kitsune.domain.manager.library.LibraryManager
import io.github.drumber.kitsune.domain.manager.library.SynchronizationResult
import io.github.drumber.kitsune.domain.mapper.toLibraryEntry
import io.github.drumber.kitsune.domain.mapper.toLibraryEntryModification
import io.github.drumber.kitsune.domain.mapper.toLocalLibraryEntry
import io.github.drumber.kitsune.domain.mapper.toLocalLibraryEntryModification
import io.github.drumber.kitsune.domain.model.database.LocalLibraryEntryModification
import io.github.drumber.kitsune.domain.model.infrastructure.library.LibraryEntry
import io.github.drumber.kitsune.domain.model.ui.library.LibraryEntryModification
import io.github.drumber.kitsune.domain.model.ui.library.LibraryEntryWrapper
import io.github.drumber.kitsune.domain.service.Filter
import io.github.drumber.kitsune.domain.service.library.LibraryEntriesService
import io.github.drumber.kitsune.util.logE
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class LibraryEditEntryViewModel(
    private val libraryManager: LibraryManager,
    private val libraryEntryDao: LibraryEntryDao,
    private val libraryModificationDao: LibraryEntryModificationDao,
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
        val oldModifiedEntry = uneditedWrapper.libraryModification
            ?.toLocalLibraryEntryModification()
            ?.applyToLibraryEntry(entry.toLocalLibraryEntry())
            ?: entry
        // apply new modifications
        val newModifiedEntry = it.libraryModification
            ?.toLocalLibraryEntryModification()
            ?.applyToLibraryEntry(entry.toLocalLibraryEntry())
            ?: entry
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
                libraryModificationDao.getLibraryEntryModification(libraryEntryId)
                    ?: LocalLibraryEntryModification.withIdAndNulls(libraryEntryId)

            val libraryEntryWrapper = LibraryEntryWrapper(libraryEntry, libraryModification.toLibraryEntryModification())
            uneditedLibraryEntryWrapper = libraryEntryWrapper.copy()
            _libraryEntryWrapper.postValue(libraryEntryWrapper)
        }.invokeOnCompletion {
            _loadState.postValue(LoadState.NotLoading)
        }
    }

    private suspend fun getLibraryEntry(libraryEntryId: String): LibraryEntry? {
        return try {
            libraryEntryDao.getLibraryEntry(libraryEntryId)?.toLibraryEntry()
                ?: libraryEntriesService.getLibraryEntry(
                    libraryEntryId,
                    Filter().include("anime", "manga").options
                ).get()
        } catch (e: Exception) {
            logE("Failed to obtain library entry.", e)
            return null
        }
    }

    fun setLibraryModification(libraryModification: LibraryEntryModification) {
        libraryEntryWrapper.value
            ?.copy(libraryModification = libraryModification)
            ?.let { updatedWrapper ->
                _libraryEntryWrapper.value = updatedWrapper
            }
    }

    fun updateLibraryEntry(block: (LibraryEntryModification) -> LibraryEntryModification) {
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
                val updateResult = libraryManager.updateLibraryEntry(libraryModification)
                if (updateResult !is SynchronizationResult.Failed) {
                    _loadState.postValue(LoadState.CloseDialog)
                } else {
                    throw updateResult.exception
                }
            } catch (e: Exception) {
                logE("Failed to update library entry.", e)
                _loadState.postValue(LoadState.Error)
            }
        }
    }

    fun removeLibraryEntry() {
        val libraryEntryId = libraryEntry.value?.id ?: return
        _loadState.value = LoadState.Loading
        viewModelScope.launch(Dispatchers.IO) {
            try {
                libraryManager.removeLibraryEntry(libraryEntryId)
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