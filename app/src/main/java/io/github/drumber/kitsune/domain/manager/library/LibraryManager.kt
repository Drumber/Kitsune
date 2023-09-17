package io.github.drumber.kitsune.domain.manager.library

import io.github.drumber.kitsune.domain.mapper.toLocalLibraryEntry
import io.github.drumber.kitsune.domain.model.database.LocalLibraryEntryModification
import io.github.drumber.kitsune.domain.model.infrastructure.library.LibraryStatus
import io.github.drumber.kitsune.domain.model.infrastructure.media.BaseMedia
import io.github.drumber.kitsune.exception.NotFoundException
import io.github.drumber.kitsune.util.logE

class LibraryManager(
    private val databaseClient: LibraryEntryDatabaseClient,
    private val serviceClient: LibraryEntryServiceClient
) {

    suspend fun addNewLibraryEntry(
        userId: String,
        media: BaseMedia,
        status: LibraryStatus
    ): Boolean {
        val libraryEntry = serviceClient.postNewLibraryEntry(userId, media, status)
            ?: return false

        databaseClient.insertLibraryEntry(libraryEntry.toLocalLibraryEntry())
        return true
    }

    suspend fun removeLibraryEntry(libraryEntryId: String): Boolean {
        val isDeleted = serviceClient.deleteLibraryEntry(libraryEntryId)
        if (isDeleted) {
            databaseClient.deleteLibraryEntryAndAnyModification(libraryEntryId)
            return true
        }
        return false
    }

    /**
     * Check if library entry was deleted on the server. If so, remove it from local database.
     */
    suspend fun mayRemoveLibraryEntryLocally(libraryEntryId: String) {
        if (!serviceClient.doesLibraryEntryExist(libraryEntryId)) {
            databaseClient.deleteLibraryEntryAndAnyModification(libraryEntryId)
        }
    }

    suspend fun synchronizeLocalModificationWithService(
        libraryEntryModification: LocalLibraryEntryModification
    ): SynchronizationResult {
        databaseClient.insertLibraryEntryModification(libraryEntryModification)

        val libraryEntryResponse = try {
            serviceClient.updateLibraryEntryWithModification(libraryEntryModification)
        } catch (e: NotFoundException) {
            logE(
                "Cannot synchronize local library entry modification for removed library entry.",
                e
            )
            return SynchronizationResult.NOT_FOUND
        } ?: return SynchronizationResult.FAILED

        databaseClient.updateLibraryEntryAndDeleteModification(
            libraryEntryResponse.toLocalLibraryEntry(),
            libraryEntryModification
        )
        return SynchronizationResult.SUCCESS
    }

    /**
     * Synchronize all locally stored library entry modifications.
     *
     * @return Map containing the [SynchronizationResult] for each library entry modification.
     */
    suspend fun synchronizeStoredLocalModificationsWithService(): Map<String, SynchronizationResult> {
        return databaseClient.getAllLocalLibraryModifications()
            .associate { libraryEntryModification ->
                libraryEntryModification.id to synchronizeLocalModificationWithService(
                    libraryEntryModification
                )
            }
    }

}