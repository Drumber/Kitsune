package io.github.drumber.kitsune.data.manager

import androidx.room.withTransaction
import com.github.jasminb.jsonapi.JSONAPIDocument
import io.github.drumber.kitsune.data.model.library.LibraryEntry
import io.github.drumber.kitsune.data.model.library.OfflineLibraryUpdate
import io.github.drumber.kitsune.data.model.library.toOfflineLibraryUpdate
import io.github.drumber.kitsune.data.room.ResourceDatabase
import io.github.drumber.kitsune.data.service.library.LibraryEntriesService
import io.github.drumber.kitsune.exception.ReceivedDataException
import io.github.drumber.kitsune.util.logD
import io.github.drumber.kitsune.util.logE
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class LibraryManager(
    private val libraryEntriesService: LibraryEntriesService,
    private val database: ResourceDatabase
) {

    private val libraryEntryDao = database.libraryEntryDao()
    private val offlineLibraryUpdateDao = database.offlineLibraryEntryDao()

    suspend fun updateProgress(
        oldEntry: LibraryEntry,
        newProgress: Int?,
        errorCallback: (e: Exception) -> Unit
    ) {
        val offlineEntry = offlineLibraryUpdateDao.getOfflineLibraryUpdate(oldEntry.id)

        val updatedEntry = offlineEntry?.toLibraryEntry()?.apply {
            // update offline entry with new progress
            progress = newProgress
        } ?: LibraryEntry(
            id = oldEntry.id,
            progress = newProgress
        )

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

                if (offlineEntry != null) {
                    // remove offline library entry since we successfully synced with the server
                    database.withTransaction {
                        offlineLibraryUpdateDao.deleteOfflineLibraryUpdate(offlineEntry)
                    }
                }
            } ?: throw ReceivedDataException("Received data for updated progress is 'null'.")
        } catch (e: Exception) {
            logE("Failed to update library entry progress.", e)
            updateOfflineLibraryEntry(offlineEntry, updatedEntry)
            withContext(Dispatchers.Main) {
                errorCallback.invoke(e)
            }
        }
    }


    suspend fun updateRating(
        oldEntry: LibraryEntry,
        rating: Int?,
        errorCallback: (e: Exception) -> Unit
    ) {
        if (rating != null && rating !in 2..20) {
            errorCallback.invoke(IllegalArgumentException("Rating must be in range 2..20."))
            return
        }

        val updatedRating = rating ?: -1 // '-1' will be mapped to 'null' by the json serializer
        val offlineEntry = offlineLibraryUpdateDao.getOfflineLibraryUpdate(oldEntry.id)

        val updatedEntry = offlineEntry?.toLibraryEntry()?.apply {
            ratingTwenty = updatedRating
        } ?: LibraryEntry(
            id = oldEntry.id,
            ratingTwenty = updatedRating
        )

        try {
            val response = libraryEntriesService.updateLibraryEntry(
                updatedEntry.id,
                JSONAPIDocument(updatedEntry)
            )

            response.get()?.let { newEntry ->
                libraryEntryDao.updateLibraryEntry(
                    newEntry.copy(
                        anime = oldEntry.anime,
                        manga = oldEntry.manga
                    )
                )

                if (offlineEntry != null) {
                    database.withTransaction {
                        offlineLibraryUpdateDao.deleteOfflineLibraryUpdate(offlineEntry)
                    }
                }
            } ?: throw ReceivedDataException("Received data for updated rating is 'null'.")
        } catch (e: Exception) {
            logE("Failed to update library entry rating.", e)
            updateOfflineLibraryEntry(offlineEntry, updatedEntry)
            withContext(Dispatchers.Main) {
                errorCallback.invoke(e)
            }
        }
    }

    private suspend fun updateOfflineLibraryEntry(
        offlineEntry: OfflineLibraryUpdate?,
        updatedEntry: LibraryEntry
    ) {
        val newOfflineUpdate = updatedEntry.toOfflineLibraryUpdate()
        database.withTransaction {
            if (offlineEntry != null) {
                logD("Update offline library entry: (old) $offlineEntry -> (new) $newOfflineUpdate")
                offlineLibraryUpdateDao.updateOfflineLibraryUpdate(newOfflineUpdate)
            } else {
                logD("Insert new offline library entry: $newOfflineUpdate")
                offlineLibraryUpdateDao.insertSingle(newOfflineUpdate)
            }
        }
    }

}