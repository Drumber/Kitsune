package io.github.drumber.kitsune.ui.details

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.drumber.kitsune.data.model.library.LibraryEntry
import io.github.drumber.kitsune.data.model.resource.ResourceAdapter
import io.github.drumber.kitsune.data.repository.UserRepository
import io.github.drumber.kitsune.data.room.LibraryEntryDao
import io.github.drumber.kitsune.data.service.Filter
import io.github.drumber.kitsune.data.service.library.LibraryEntriesService
import io.github.drumber.kitsune.util.logE
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class DetailsViewModel(
    private val userRepository: UserRepository,
    private val libraryEntriesService: LibraryEntriesService,
    private val libraryEntryDao: LibraryEntryDao
) : ViewModel() {

    private val _resourceAdapter = MutableLiveData<ResourceAdapter>()
    val resourceAdapter: LiveData<ResourceAdapter>
        get() = _resourceAdapter

    private val _libraryEntry = MutableLiveData<LibraryEntry>()
    val libraryEntry: LiveData<LibraryEntry>
        get() = _libraryEntry

    fun initResourceAdapter(resourceAdapter: ResourceAdapter) {
        if (_resourceAdapter.value == null) {
            _resourceAdapter.value = resourceAdapter
            loadLibraryEntry(resourceAdapter)
        }
    }

    private fun loadLibraryEntry(resourceAdapter: ResourceAdapter) {
        val userId = userRepository.user?.id ?: return

        val filter = Filter()
            .filter("user_id", userId)
            .fields("libraryEntries", "status", "progress")
            .pageLimit(1)

        if (resourceAdapter.isAnime()) {
            filter.filter("anime_id", resourceAdapter.id)
        } else {
            filter.filter("manga_id", resourceAdapter.id)
        }

        viewModelScope.launch(Dispatchers.IO) {
            // check if library entry is cached in local database first
            val libraryEntry = libraryEntryDao.getLibraryEntryFromResource(resourceAdapter.id)
            libraryEntry?.let { _libraryEntry.postValue(it) }

            try {
                // fetch library entry from the server
                val libraryEntries = libraryEntriesService.allLibraryEntries(filter.options).get()
                if (!libraryEntries.isNullOrEmpty()) {
                    _libraryEntry.postValue(libraryEntries[0])
                }
            } catch (e: Exception) {
                logE("Failed to load library entry.", e)
            }
        }
    }

}