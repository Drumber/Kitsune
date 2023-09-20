package io.github.drumber.kitsune.domain.manager.library

import io.github.drumber.kitsune.domain.manager.library.SynchronizationResult.Failed
import io.github.drumber.kitsune.domain.manager.library.SynchronizationResult.NotFound
import io.github.drumber.kitsune.domain.manager.library.SynchronizationResult.Success
import io.github.drumber.kitsune.domain.mapper.toLocalLibraryEntry
import io.github.drumber.kitsune.domain.mapper.toLocalLibraryEntryModification
import io.github.drumber.kitsune.domain.model.database.LocalLibraryEntryModification
import io.github.drumber.kitsune.domain.model.database.LocalLibraryModificationState
import io.github.drumber.kitsune.domain.model.database.LocalLibraryModificationState.SYNCHRONIZING
import io.github.drumber.kitsune.domain.model.infrastructure.library.LibraryEntry
import io.github.drumber.kitsune.domain.model.infrastructure.library.LibraryStatus
import io.github.drumber.kitsune.domain.model.infrastructure.media.BaseMedia
import io.github.drumber.kitsune.domain.model.ui.library.LibraryEntryModification
import io.github.drumber.kitsune.exception.InvalidDataException
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
    ): LibraryEntry? {
        val libraryEntry = serviceClient.postNewLibraryEntry(userId, media, status)
        if (libraryEntry != null) {
            databaseClient.insertLibraryEntry(libraryEntry.toLocalLibraryEntry())
        }
        return libraryEntry
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

    suspend fun updateLibraryEntry(
        libraryEntryModification: LibraryEntryModification
    ): SynchronizationResult {
        return updateLibraryEntry(libraryEntryModification.toLocalLibraryEntryModification())
    }

    suspend fun updateLibraryEntry(
        libraryEntryModification: LocalLibraryEntryModification
    ): SynchronizationResult {
        val localModification = libraryEntryModification.copy(state = SYNCHRONIZING)

        databaseClient.insertLibraryEntryModification(localModification)
        val syncResult = pushLocalModificationToService(localModification)
        when (syncResult) {
            is Success -> {
                databaseClient.updateLibraryEntryAndDeleteModification(
                    syncResult.libraryEntry.toLocalLibraryEntry(),
                    localModification
                )
            }

            is Failed -> {
                insertLocalModificationOrDeleteIfSameAsLibraryEntry(
                    localModification.copy(state = LocalLibraryModificationState.NOT_SYNCHRONIZED)
                )
            }

            is NotFound -> {
                databaseClient.deleteLibraryEntryAndAnyModification(localModification.id)
            }
        }

        return syncResult
    }

    /**
     * Synchronize all locally stored library entry modifications.
     *
     * @return Map containing the [SynchronizationResult] for each library entry modification.
     */
    suspend fun pushAllStoredLocalModificationsToService(): Map<String, SynchronizationResult> {
        return databaseClient.getAllLocalLibraryModifications()
            .associate { libraryEntryModification ->
                libraryEntryModification.id to updateLibraryEntry(
                    libraryEntryModification
                )
            }
    }

    private suspend fun pushLocalModificationToService(
        libraryEntryModification: LocalLibraryEntryModification
    ): SynchronizationResult {
        val libraryEntryResponse = try {
            serviceClient.updateLibraryEntryWithModification(libraryEntryModification)
                ?: throw InvalidDataException("Received library entry for ID '${libraryEntryModification.id}' is 'null'.")
        } catch (e: NotFoundException) {
            logE(
                "Cannot synchronize local library entry modification for removed library entry.",
                e
            )
            return NotFound
        } catch (e: Exception) {
            logE(
                "Failed to push library entry modification for ID '${libraryEntryModification.id}' to service.",
                e
            )
            return Failed(e)
        }
        return Success(libraryEntryResponse)
    }

    private suspend fun insertLocalModificationOrDeleteIfSameAsLibraryEntry(
        libraryEntryModification: LocalLibraryEntryModification
    ) {
        val libraryEntry = databaseClient.getLibraryEntry(libraryEntryModification.id)
        if (libraryEntry != null && libraryEntryModification.isEqualToLibraryEntry(libraryEntry)) {
            databaseClient.deleteLibraryEntryModification(libraryEntryModification)
        } else {
            databaseClient.insertLibraryEntryModification(libraryEntryModification)
        }
    }

}