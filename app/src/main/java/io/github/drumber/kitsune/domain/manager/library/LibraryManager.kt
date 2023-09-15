package io.github.drumber.kitsune.domain.manager.library

import io.github.drumber.kitsune.domain.mapper.toLocalLibraryEntry
import io.github.drumber.kitsune.domain.model.database.LocalLibraryEntry
import io.github.drumber.kitsune.domain.model.database.LocalLibraryEntryModification
import io.github.drumber.kitsune.domain.model.infrastructure.library.LibraryStatus
import io.github.drumber.kitsune.domain.model.infrastructure.media.BaseMedia

class LibraryManager(
    private val databaseClient: LibraryEntryDatabaseClient,
    private val serviceClient: LibraryEntryServiceClient,
    private val shouldStoreOfflineModifications: () -> Boolean
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

    suspend fun removeLibraryEntry(libraryEntry: LocalLibraryEntry): Boolean {
        val isDeleted = serviceClient.deleteLibraryEntry(libraryEntry.id)
        if (isDeleted) {
            databaseClient.deleteLibraryEntryAndAnyModification(libraryEntry)
            return true
        }
        return false
    }

    /**
     * Check if library entry was deleted on the server. If so, remove it from local database.
     */
    suspend fun mayRemoveLibraryEntryLocally(libraryEntry: LocalLibraryEntry) {
        if (!serviceClient.doesLibraryEntryExist(libraryEntry.id)) {
            databaseClient.deleteLibraryEntryAndAnyModification(libraryEntry)
        }
    }

    suspend fun synchronizeLocalModificationWithService(
        libraryEntryModification: LocalLibraryEntryModification
    ): Boolean {
        val libraryEntryResponse =
            serviceClient.updateLibraryEntryWithModification(libraryEntryModification)

        if (libraryEntryResponse == null && shouldStoreOfflineModifications()) {
            databaseClient.insertLibraryEntryModification(libraryEntryModification)
            return false
        } else if (libraryEntryResponse == null) {
            return false
        }

        databaseClient.updateLibraryEntryAndDeleteModification(
            libraryEntryResponse.toLocalLibraryEntry(),
            libraryEntryModification
        )
        return true
    }

}