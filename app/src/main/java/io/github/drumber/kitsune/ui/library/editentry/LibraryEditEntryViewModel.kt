package io.github.drumber.kitsune.ui.library.editentry

import androidx.lifecycle.*
import io.github.drumber.kitsune.data.manager.LibraryManager
import io.github.drumber.kitsune.data.model.library.LibraryEntry
import io.github.drumber.kitsune.data.model.library.LibraryEntryWrapper
import io.github.drumber.kitsune.data.model.library.LibraryModification
import io.github.drumber.kitsune.data.room.LibraryEntryDao
import io.github.drumber.kitsune.data.room.OfflineLibraryModificationDao
import io.github.drumber.kitsune.data.service.Filter
import io.github.drumber.kitsune.data.service.library.LibraryEntriesService
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

        viewModelScope.launch(Dispatchers.IO) {
            val libraryEntry = getLibraryEntry(libraryEntryId) ?: return@launch
            _libraryEntry.postValue(libraryEntry)

            val libraryModification =
                libraryModificationDao.getOfflineLibraryModification(libraryEntryId)
                    ?: LibraryModification(libraryEntryId)

            val libraryEntryWrapper = LibraryEntryWrapper(libraryEntry, libraryModification)
            uneditedLibraryEntryWrapper = libraryEntryWrapper.copy()
            _libraryEntryWrapper.postValue(libraryEntryWrapper)
        }
    }

    private suspend fun getLibraryEntry(libraryEntryId: String): LibraryEntry? {
        return libraryEntryDao.getLibraryEntry(libraryEntryId)
            ?: libraryEntriesService.getLibraryEntry(
                libraryEntryId,
                Filter().include("anime", "manga").options
            ).get()
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
        // TODO
    }

    fun removeLibraryEntry() {
        // TODO
    }

}