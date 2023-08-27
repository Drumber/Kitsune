package io.github.drumber.kitsune.domain.manager

import androidx.room.withTransaction
import com.github.jasminb.jsonapi.JSONAPIDocument
import io.github.drumber.kitsune.domain.database.LocalDatabase
import io.github.drumber.kitsune.domain.mapper.toLibraryEntry
import io.github.drumber.kitsune.domain.mapper.toLocalLibraryEntry
import io.github.drumber.kitsune.domain.model.database.LocalLibraryEntry
import io.github.drumber.kitsune.domain.model.database.LocalLibraryEntryModification
import io.github.drumber.kitsune.domain.model.infrastructure.library.LibraryEntry
import io.github.drumber.kitsune.domain.model.infrastructure.library.LibraryStatus
import io.github.drumber.kitsune.domain.model.infrastructure.media.Anime
import io.github.drumber.kitsune.domain.model.infrastructure.media.BaseMedia
import io.github.drumber.kitsune.domain.model.infrastructure.media.Manga
import io.github.drumber.kitsune.domain.model.infrastructure.user.User
import io.github.drumber.kitsune.domain.service.Filter
import io.github.drumber.kitsune.domain.service.library.LibraryEntriesService
import io.github.drumber.kitsune.exception.InvalidDataException
import io.github.drumber.kitsune.preference.KitsunePref
import io.github.drumber.kitsune.util.DATE_FORMAT_ISO
import io.github.drumber.kitsune.util.formatDate
import io.github.drumber.kitsune.util.logD
import io.github.drumber.kitsune.util.logE
import io.github.drumber.kitsune.util.logI
import io.github.drumber.kitsune.util.logW
import io.github.drumber.kitsune.util.toDate
import io.github.drumber.kitsune.util.todayUtcMillis
import retrofit2.HttpException

class LibraryManager(
    private val libraryEntriesService: LibraryEntriesService,
    private val database: LocalDatabase
) {

    private val libraryEntryDao = database.libraryEntryDao()
    private val libraryModificationDao = database.libraryEntryModificationDao()

    private val isOfflineCacheEnabled
        get() = KitsunePref.libraryOfflineSync

    private var isSynchronizationInProgress = false

    suspend fun synchronizeLibrary(responseCallback: (LibraryUpdateResponse) -> Unit) {
        if (isSynchronizationInProgress) return
        isSynchronizationInProgress = true

        try {
            val offlineModifications =
                libraryModificationDao.getAllLibraryEntryModifications()

            logD("Synchronizing ${offlineModifications.size} offline modifications...")

            offlineModifications.forEach { libraryModification ->
                val modifiedLibraryEntry = libraryModification.toLocalLibraryEntry()

                try {
                    // post library update to server
                    val responseLibraryEntry = libraryEntriesService.updateLibraryEntry(
                        modifiedLibraryEntry.id,
                        JSONAPIDocument(modifiedLibraryEntry.toLibraryEntry())
                    ).get() ?: throw InvalidDataException("Received library entry is 'null'.")

                    logI("Successfully synchronized offline library modification, removing modification from database: ${libraryModification.id}")
                    // remove offline library modification from database
                    database.withTransaction {
                        libraryModificationDao.deleteLibraryEntryModification(
                            libraryModification
                        )
                    }

                    // update library entry in database
                    updateLibraryEntryInDatabase(responseLibraryEntry.toLocalLibraryEntry())
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
        status: LibraryStatus = LibraryStatus.Planned
    ): LibraryEntry? {
        var libraryEntry = LibraryEntry.withNulls().copy(
            status = status,
            user = User(id = userId)
        )
        libraryEntry = when (media) {
            is Anime -> libraryEntry.copy(anime = media)
            is Manga -> libraryEntry.copy(manga = media)
        }

        return try {
            val response = libraryEntriesService.postLibraryEntry(JSONAPIDocument(libraryEntry))
            val responseLibraryEntry = response.get()
                ?: throw InvalidDataException("Response library entry is 'null'.")
            val responseLibraryEntryId = responseLibraryEntry.id
                ?: throw InvalidDataException("The ID of the response library entry is 'null'.")

            // fetch full library entry and add it to the db
            val fullLibraryEntry = fetchFullLibraryEntry(responseLibraryEntryId)
                ?: throw InvalidDataException("Full library entry is 'null'.")

            database.withTransaction {
                libraryEntryDao.insertSingle(fullLibraryEntry.toLocalLibraryEntry())
            }

            logI("Added new library entry to local database: ${fullLibraryEntry.id}")

            fullLibraryEntry
        } catch (e: Exception) {
            logE("Failed to post new library entry.", e)
            null
        }
    }

    suspend fun updateLibraryEntry(libraryModification: LocalLibraryEntryModification): LibraryUpdateResponse {
        var modification = libraryModification.applyFixes()

        // check if there is an existing library modification
        val existingModification =
            libraryModificationDao.getLibraryEntryModification(libraryModification.id)
        if (existingModification != null) {
            // merge with changes from existing library modification
            modification = existingModification.mergeModificationFrom(libraryModification)
        }

        return updateLibraryEntryIntern(modification, existingModification != null)
    }

    suspend fun removeLibraryEntry(libraryEntry: LocalLibraryEntry) {
        libraryEntriesService.deleteLibraryEntry(libraryEntry.id)
        // remove any offline modification and the library entry itself from database
        removeFromDatabaseIntern(libraryEntry)
    }

    /**
     * Check if library entry was deleted on the server. If so, remove it from local database.
     */
    suspend fun mayRemoveSingleLibraryEntry(libraryEntry: LocalLibraryEntry) {
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

    private suspend fun removeFromDatabaseIntern(libraryEntry: LocalLibraryEntry) {
        database.withTransaction {
            libraryModificationDao.getLibraryEntryModification(libraryEntry.id)?.let {
                libraryModificationDao.deleteLibraryEntryModification(it)
            }
            libraryEntryDao.delete(libraryEntry)
        }
        logI("Removed library entry from local database: ${libraryEntry.id}")
    }

    private suspend fun updateLibraryEntryIntern(
        modification: LocalLibraryEntryModification,
        isExistingModification: Boolean
    ): LibraryUpdateResponse {
        val modifiedLibraryEntry = modification.toLocalLibraryEntry()

        val responseLibraryEntry = try {
            // post library update to server
            val response = libraryEntriesService.updateLibraryEntry(
                modifiedLibraryEntry.id,
                JSONAPIDocument(modifiedLibraryEntry.toLibraryEntry())
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
                    libraryModificationDao.deleteLibraryEntryModification(modification)
                }
                logD("Removed existing offline library modification for ${modification.id}")
            }

            updateLibraryEntryInDatabase(responseLibraryEntry.toLocalLibraryEntry())

            LibraryUpdateResponse.SyncedOnline
        } else { // update failed: cache modifications
            database.withTransaction {
                if (isExistingModification) {
                    // update existing modification
                    libraryModificationDao.updateLibraryEntryModification(modification)
                    logD("Updated offline library modification: $modification")
                } else {
                    // insert new modification
                    libraryModificationDao.insertSingle(modification)
                    logD("Inserted new offline library modification: $modification")
                }
            }
            LibraryUpdateResponse.OfflineCache
        }
    }

    private suspend fun updateLibraryEntryInDatabase(libraryEntry: LocalLibraryEntry) {
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
                    libraryEntryDao.insertSingle(fetchedFullLibraryEntry.toLocalLibraryEntry())
                }
                logD("Inserted library entry into database: ${fetchedFullLibraryEntry.id}")
            } else {
                logW("Library Entry was not inserted into database due to failed fetch of full model: ${libraryEntry.id}")
            }
        }
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

    /**
     * Apply fixes to the library modification before submitting it to the server.
     */
    private fun LocalLibraryEntryModification.applyFixes() = this.copy(
        // Fix for setting startedAt date when user starts consuming the media.
        // startedAt is only set if status is CURRENT or COMPLETED, see here:
        // https://github.com/hummingbird-me/kitsu-server/blob/703726fc84a1a0172eae9a55c751ae6ffb1665b3/app/models/library_entry.rb#L204
        // Since we don't know if the media has only 1 unit, we set the startedAt date instead of the status.
        startedAt = if (startedAt == null && progress == 1)
            todayUtcMillis().toDate().formatDate(DATE_FORMAT_ISO)
        else
            startedAt
    )

}
