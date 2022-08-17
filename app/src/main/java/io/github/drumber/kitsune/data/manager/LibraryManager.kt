package io.github.drumber.kitsune.data.manager

import androidx.room.withTransaction
import com.github.jasminb.jsonapi.JSONAPIDocument
import io.github.drumber.kitsune.data.model.library.LibraryEntry
import io.github.drumber.kitsune.data.model.library.LibraryModification
import io.github.drumber.kitsune.data.room.ResourceDatabase
import io.github.drumber.kitsune.data.service.library.LibraryEntriesService
import io.github.drumber.kitsune.exception.InvalidDataException
import io.github.drumber.kitsune.exception.ReceivedDataException
import io.github.drumber.kitsune.preference.KitsunePref
import io.github.drumber.kitsune.util.logD
import io.github.drumber.kitsune.util.logE
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class LibraryManager(
    private val libraryEntriesService: LibraryEntriesService,
    private val database: ResourceDatabase
) {

    private val libraryEntryDao = database.libraryEntryDao()
    private val offlineLibraryModificationDao = database.offlineLibraryModificationDao()

    private val isOfflineCacheEnabled
        get() = KitsunePref.libraryOfflineSync

    suspend fun synchronizeLibrary(responseCallback: ResponseCallback) {
        val offlineModifications = offlineLibraryModificationDao.getAllOfflineLibraryModifications()

        offlineModifications.forEach { offlineLibraryModification ->
            val updatedEntry = offlineLibraryModification.toLibraryEntry()

            try {
                val response = libraryEntriesService.updateLibraryEntry(
                    updatedEntry.id,
                    JSONAPIDocument(updatedEntry)
                )

                response.get()?.let { libraryEntry ->
                    // delete old offline library update from database
                    offlineLibraryModificationDao.deleteOfflineLibraryModification(offlineLibraryModification)

                    // update library entry database, therefore we need the full library entry object
                    database.libraryEntryDao().getLibraryEntry(libraryEntry.id)?.let { oldEntry ->
                        libraryEntryDao.updateLibraryEntry(
                            libraryEntry.copy(
                                anime = oldEntry.anime,
                                manga = oldEntry.manga
                            )
                        )
                    } ?: throw InvalidDataException("Cannot update library database due to missing old library entity.")
                } ?: throw ReceivedDataException("Received data for updated progress is 'null'.")
            } catch (e: Exception) {
                logE("Failed to synchronize library entry ${offlineLibraryModification.id}", e)
                responseCallback.call(LibraryUpdateResponse.Error(e))
            }
        }
        responseCallback.call(LibraryUpdateResponse.SyncedOnline)
    }

    suspend fun updateProgress(
        oldEntry: LibraryEntry,
        newProgress: Int?,
        responseCallback: ResponseCallback
    ) {
        val libraryModification = offlineLibraryModificationDao.getOfflineLibraryModification(oldEntry.id)?.copy(
            progress = newProgress // apply new progress
        )

        val updatedEntry = libraryModification?.toLibraryEntry() ?: LibraryEntry(
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

                if (libraryModification != null) {
                    // remove offline library modification since we successfully synced with the server
                    database.withTransaction {
                        offlineLibraryModificationDao.deleteOfflineLibraryModification(libraryModification)
                    }
                }

                responseCallback.invoke(LibraryUpdateResponse.SyncedOnline)
            } ?: throw ReceivedDataException("Received data for updated progress is 'null'.")
        } catch (e: Exception) {
            logE("Failed to update library entry progress.", e)
            if (isOfflineCacheEnabled) {
                // update or insert offline library modification
                database.withTransaction {
                    if (libraryModification != null) {
                        logD("Update offline library modification: $libraryModification")
                        offlineLibraryModificationDao.updateOfflineLibraryModification(libraryModification)
                    } else {
                        val newOfflineModification = LibraryModification(
                            id = updatedEntry.id,
                            ratingTwenty = newProgress
                        )
                        logD("Insert new offline library modification: $libraryModification")
                        offlineLibraryModificationDao.insertSingle(newOfflineModification)
                    }
                }
                responseCallback.call(LibraryUpdateResponse.OfflineCache)
            } else {
                responseCallback.call(LibraryUpdateResponse.Error(e))
            }
        }
    }


    suspend fun updateRating(
        oldEntry: LibraryEntry,
        rating: Int?,
        responseCallback: ResponseCallback
    ) {
        if (rating != null && rating !in 2..20) {
            responseCallback.call(
                LibraryUpdateResponse.Error(
                    IllegalArgumentException("Rating must be in range 2..20.")
                )
            )
            return
        }

        val updatedRating = rating ?: -1 // '-1' will be mapped to 'null' by the json serializer
        val libraryModification = offlineLibraryModificationDao.getOfflineLibraryModification(oldEntry.id)?.copy(
            ratingTwenty = updatedRating // apply new rating
        )

        val updatedEntry = libraryModification?.toLibraryEntry() ?: LibraryEntry(
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

                if (libraryModification != null) {
                    database.withTransaction {
                        offlineLibraryModificationDao.deleteOfflineLibraryModification(libraryModification)
                    }
                }
            } ?: throw ReceivedDataException("Received data for updated rating is 'null'.")

            responseCallback.call(LibraryUpdateResponse.SyncedOnline)
        } catch (e: Exception) {
            logE("Failed to update library entry rating.", e)

            if (isOfflineCacheEnabled) {
                // update or insert offline library modification
                database.withTransaction {
                    if (libraryModification != null) {
                        logD("Update offline library modification: $libraryModification")
                        offlineLibraryModificationDao.updateOfflineLibraryModification(libraryModification)
                    } else {
                        val newOfflineModification = LibraryModification(
                            id = updatedEntry.id,
                            ratingTwenty = updatedRating
                        )
                        logD("Insert new offline library modification: $libraryModification")
                        offlineLibraryModificationDao.insertSingle(newOfflineModification)
                    }
                }
                responseCallback.call(LibraryUpdateResponse.OfflineCache)
            } else {
                responseCallback.call(LibraryUpdateResponse.Error(e))
            }
        }
    }

    private suspend fun ResponseCallback.call(response: LibraryUpdateResponse) {
        withContext(Dispatchers.Main) {
            this@call.invoke(response)
        }
    }

}

typealias ResponseCallback = (LibraryUpdateResponse) -> Unit
