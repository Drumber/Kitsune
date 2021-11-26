package io.github.drumber.kitsune.ui.base

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.jasminb.jsonapi.JSONAPIDocument
import io.github.drumber.kitsune.data.model.library.LibraryEntry
import io.github.drumber.kitsune.data.room.LibraryEntryDao
import io.github.drumber.kitsune.data.service.library.LibraryEntriesService
import io.github.drumber.kitsune.exception.ReceivedDataException
import io.github.drumber.kitsune.util.logE
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

abstract class BaseLibraryViewModel(
    protected val libraryEntriesService: LibraryEntriesService,
    protected val libraryEntryDao: LibraryEntryDao
) : ViewModel() {

    protected fun updateLibraryProgress(
        oldEntry: LibraryEntry,
        newProgress: Int?,
        errorCallback: (e: Exception) -> Unit
    ) {
        val updatedEntry = LibraryEntry(
            id = oldEntry.id,
            progress = newProgress
        )

        viewModelScope.launch(Dispatchers.IO) {
            try {
                val response = libraryEntriesService.updateLibraryEntry(
                    updatedEntry.id,
                    JSONAPIDocument(updatedEntry)
                )

                response.get()?.let { libraryEntry ->
                    // update the database, but copy anime and manga object from old library first
                    libraryEntryDao.updateLibraryEntry(
                        libraryEntry.copy(
                            anime = oldEntry.anime,
                            manga = oldEntry.manga
                        )
                    )
                } ?: throw ReceivedDataException("Received data is 'null'.")
            } catch (e: Exception) {
                logE("Failed to update library entry progress.", e)
                withContext(Dispatchers.Main) {
                    errorCallback.invoke(e)
                }
            }
        }
    }

}