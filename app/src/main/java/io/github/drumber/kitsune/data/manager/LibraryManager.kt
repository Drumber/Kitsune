package io.github.drumber.kitsune.data.manager

import androidx.room.withTransaction
import com.github.jasminb.jsonapi.JSONAPIDocument
import io.github.drumber.kitsune.data.model.library.LibraryEntry
import io.github.drumber.kitsune.data.model.library.LibraryModification
import io.github.drumber.kitsune.data.model.library.Status
import io.github.drumber.kitsune.data.model.media.Anime
import io.github.drumber.kitsune.data.model.media.BaseMedia
import io.github.drumber.kitsune.data.model.media.Manga
import io.github.drumber.kitsune.data.model.user.User
import io.github.drumber.kitsune.data.room.ResourceDatabase
import io.github.drumber.kitsune.data.service.Filter
import io.github.drumber.kitsune.data.service.library.LibraryEntriesService
import io.github.drumber.kitsune.exception.InvalidDataException
import io.github.drumber.kitsune.preference.KitsunePref
import io.github.drumber.kitsune.util.logD
import io.github.drumber.kitsune.util.logE
import io.github.drumber.kitsune.util.logI
import io.github.drumber.kitsune.util.logW
import retrofit2.HttpException

class LibraryManager(
    private val libraryEntriesService: LibraryEntriesService,
    private val database: ResourceDatabase
) {

    private val libraryEntryDao = database.libraryEntryDao()
    private val offlineLibraryModificationDao = database.offlineLibraryModificationDao()

    private val isOfflineCacheEnabled
        get() = KitsunePref.libraryOfflineSync

    private var isSynchronizationInProgress = false

    suspend fun synchronizeLibrary(responseCallback: (LibraryUpdateResponse) -> Unit) {
        if (isSynchronizationInProgress) return
        isSynchronizationInProgress = true

        try {
            val offlineModifications =
                offlineLibraryModificationDao.getAllOfflineLibraryModifications()

            logD("Synchronizing ${offlineModifications.size} offline modifications...")

            offlineModifications.forEach { libraryModification ->
                val modifiedLibraryEntry = libraryModification.toLibraryEntry()

                try {
                    // post library update to server
                    val responseLibraryEntry = libraryEntriesService.updateLibraryEntry(
                        modifiedLibraryEntry.id,
                        JSONAPIDocument(modifiedLibraryEntry)
                    ).get() ?: throw InvalidDataException("Received library entry is 'null'.")

                    logI("Successfully synchronized offline library modification, removing modification from database: ${libraryModification.id}")
                    // remove offline library modification from database
                    database.withTransaction {
                        offlineLibraryModificationDao.deleteOfflineLibraryModification(
                            libraryModification
                        )
                    }

                    // update library entry in database
                    updateLibraryEntryInDatabase(responseLibraryEntry)
                } catch (e: Exception) {
                    if (e is HttpException && e.code() == 404) {
                        // library entry was removed from the server, remove is also locally
                        logW("Library entry was not found on the server. Removing it from the database: ${libraryModification.id}")
                        removeFromDatabaseIntern(modifiedLibraryEntry)
                    } else {
                        logE("Failed to synchronize library entry ${libraryModification.id}", e)
                    }
                    responseCallback(LibraryUpdateResponse.Error(e))
                }
            }

            logD("Finished synchronizing offline modifications.")
            responseCallback(LibraryUpdateResponse.SyncedOnline)
        } catch (e: Exception) {
            logE("Something went wrong while synchronizing offline modifications.", e)
        } finally {
            isSynchronizationInProgress = false
        }
    }

    suspend fun postNewLibraryEntry(
        userId: String,
        media: BaseMedia,
        status: Status = Status.Planned
    ): LibraryEntry? {
        val libraryEntry = LibraryEntry(status = status)
        libraryEntry.user = User(id = userId)
        when (media) {
            is Anime -> libraryEntry.anime = media
            is Manga -> libraryEntry.manga = media
        }

        return try {
            val response = libraryEntriesService.postLibraryEntry(JSONAPIDocument(libraryEntry))
            val responseLibraryEntry = response.get()
                ?: throw InvalidDataException("Response library entry is 'null'.")

            // fetch full library entry and add it to the db
            val fullLibraryEntry = fetchFullLibraryEntry(responseLibraryEntry.id)
                ?: throw InvalidDataException("Full library entry is 'null'.")

            database.withTransaction {
                libraryEntryDao.insertSingle(fullLibraryEntry)
            }

            logI("Added new library entry to local database: ${fullLibraryEntry.id}")

            fullLibraryEntry
        } catch (e: Exception) {
            logE("Failed to post new library entry.", e)
            null
        }
    }

    suspend fun removeLibraryEntry(libraryEntry: LibraryEntry) {
        libraryEntriesService.deleteLibraryEntry(libraryEntry.id)
        // remove any offline modification and the library entry itself from database
        removeFromDatabaseIntern(libraryEntry)
    }

    /**
     * Check if library entry was deleted on the server. If so, remove it from local database.
     */
    suspend fun mayRemoveSingleLibraryEntry(libraryEntry: LibraryEntry) {
        try {
            val response = libraryEntriesService.getLibraryEntry(
                libraryEntry.id,
                Filter().fields("libraryEntries", "status").options
            )
            if (response.get() == null) {
                removeFromDatabaseIntern(libraryEntry)
            }
        } catch (e: HttpException) {
            if (e.code() == 404) {
                removeFromDatabaseIntern(libraryEntry)
            }
        } catch (e: Exception) {
            logE("Failed to check for library entry: ${libraryEntry.id}", e)
        }
    }

    private suspend fun removeFromDatabaseIntern(libraryEntry: LibraryEntry) {
        database.withTransaction {
            offlineLibraryModificationDao.getOfflineLibraryModification(libraryEntry.id)?.let {
                offlineLibraryModificationDao.deleteOfflineLibraryModification(it)
            }
            libraryEntryDao.delete(libraryEntry)
        }
        logI("Removed library entry from local database: ${libraryEntry.id}")
    }

    private suspend fun updateLibraryEntryIntern(
        modification: LibraryModification,
        isExistingModification: Boolean
    ): LibraryUpdateResponse {
        val modifiedLibraryEntry = modification.toLibraryEntry()

        val responseLibraryEntry = try {
            // post library update to server
            val response = libraryEntriesService.updateLibraryEntry(
                modifiedLibraryEntry.id,
                JSONAPIDocument(modifiedLibraryEntry)
            )
            response.get()
        } catch (e: Exception) {
            if (e is HttpException && e.code() == 404) {
                logD("Library entry was not found on the server, removing it from database: ${modification.id}")
                // library entry was removed on the server, make sure it is also removed locally
                removeFromDatabaseIntern(modifiedLibraryEntry)
                return LibraryUpdateResponse.Error(e)
            }

            logE("Failed to update library entry: ${modification.id}", e)

            if (!isOfflineCacheEnabled) {
                // offline cache is disabled, return here
                return LibraryUpdateResponse.Error(e)
            }
            null
        }

        return if (responseLibraryEntry != null) { // successful update
            // check if there was an existing library modification and remove it
            if (isExistingModification) {
                database.withTransaction {
                    offlineLibraryModificationDao.deleteOfflineLibraryModification(modification)
                }
                logD("Removed existing offline library modification for ${modification.id}")
            }

            updateLibraryEntryInDatabase(responseLibraryEntry)

            LibraryUpdateResponse.SyncedOnline
        } else { // update failed: cache modifications
            database.withTransaction {
                if (isExistingModification) {
                    // update existing modification
                    offlineLibraryModificationDao.updateOfflineLibraryModification(modification)
                    logD("Updated offline library modification: $modification")
                } else {
                    // insert new modification
                    offlineLibraryModificationDao.insertSingle(modification)
                    logD("Inserted new offline library modification: $modification")
                }
            }
            LibraryUpdateResponse.OfflineCache
        }
    }

    private suspend fun updateLibraryEntryInDatabase(libraryEntry: LibraryEntry) {
        val dbFullLibraryEntry = libraryEntryDao.getLibraryEntry(libraryEntry.id)

        // check if there is a full library entry in the database
        if (dbFullLibraryEntry != null) {
            // copy anime/manga media item to the new modified library entry
            val modifiedFullLibraryEntry = libraryEntry.copy(
                anime = dbFullLibraryEntry.anime,
                manga = dbFullLibraryEntry.manga
            )

            // update database entry
            database.withTransaction {
                libraryEntryDao.updateLibraryEntry(modifiedFullLibraryEntry)
            }
            logD("Updated library entry in database: ${dbFullLibraryEntry.id}")
        } else {
            // if not, fetch full library entry inclusive anime and manga media item
            val fetchedFullLibraryEntry = fetchFullLibraryEntry(libraryEntry.id)

            if (fetchedFullLibraryEntry != null) {
                // insert into database
                database.withTransaction {
                    libraryEntryDao.insertSingle(fetchedFullLibraryEntry)
                }
                logD("Inserted library entry into database: ${fetchedFullLibraryEntry.id}")
            }
        }
    }

    suspend fun updateLibraryEntry(libraryModification: LibraryModification): LibraryUpdateResponse {
        var modification = libraryModification

        // check if there is an existing library modification
        val existingModification =
            offlineLibraryModificationDao.getOfflineLibraryModification(libraryModification.id)
        if (existingModification != null) {
            // merge with changes from existing library modification
            modification = existingModification.mergeModificationFrom(libraryModification)
        }

        return updateLibraryEntryIntern(modification, existingModification != null)
    }

    private suspend fun fetchFullLibraryEntry(id: String): LibraryEntry? {
        return try {
            val response = libraryEntriesService.getLibraryEntry(
                id,
                Filter().include("anime", "manga").options
            )
            response.get()
        } catch (e: Exception) {
            logE("Failed to fetch full library entry: $id", e)
            null
        }
    }

}
