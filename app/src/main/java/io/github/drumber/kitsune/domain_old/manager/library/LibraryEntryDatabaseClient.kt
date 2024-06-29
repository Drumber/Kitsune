package io.github.drumber.kitsune.domain_old.manager.library

import androidx.room.withTransaction
import io.github.drumber.kitsune.domain_old.database.LocalDatabase
import io.github.drumber.kitsune.domain_old.model.database.LocalLibraryEntry
import io.github.drumber.kitsune.domain_old.model.database.LocalLibraryEntryModification

class LibraryEntryDatabaseClient(
    private val database: LocalDatabase
) {

    private val libraryEntryDao
        get() = database.libraryEntryDao()
    private val libraryEntryModificationDao
        get() = database.libraryEntryModificationDao()

    suspend fun getLibraryEntry(id: String) = libraryEntryDao.getLibraryEntry(id)

    suspend fun getAllLocalLibraryModifications(): List<LocalLibraryEntryModification> {
        return libraryEntryModificationDao.getAllLibraryEntryModifications()
    }

    suspend fun insertLibraryEntry(libraryEntry: LocalLibraryEntry) {
        libraryEntry.verifyIsValidLibraryEntry()
        libraryEntryDao.insertSingle(libraryEntry)
    }

    suspend fun updateLibraryEntry(libraryEntry: LocalLibraryEntry) {
        libraryEntryDao.updateSingle(libraryEntry)
    }

    suspend fun insertLibraryEntryModification(
        libraryEntryModification: LocalLibraryEntryModification
    ) {
        libraryEntryModificationDao.insertSingle(libraryEntryModification)
    }

    suspend fun deleteLibraryEntryModification(
        libraryEntryModification: LocalLibraryEntryModification
    ) {
        libraryEntryModificationDao.deleteSingle(libraryEntryModification)
    }

    suspend fun updateLibraryEntryAndDeleteModification(
        libraryEntry: LocalLibraryEntry,
        libraryEntryModification: LocalLibraryEntryModification
    ) {
        database.withTransaction {
            libraryEntryDao.updateSingle(libraryEntry)
            libraryEntryModificationDao.deleteSingleMatchingCreateTime(
                libraryEntryModification.id,
                libraryEntryModification.createTime
            )
        }
    }

    suspend fun deleteLibraryEntryAndAnyModification(libraryEntryId: String) {
        database.withTransaction {
            libraryEntryDao.deleteSingleById(libraryEntryId)
            libraryEntryModificationDao.deleteSingleById(libraryEntryId)
        }
    }

    /**
     * Verifies that the library entry has an ID and contains a media object.
     *
     * @throws IllegalArgumentException if the library entry is not valid
     */
    private fun LocalLibraryEntry.verifyIsValidLibraryEntry() {
        requireNotNull(this.id)
        require(this.anime != null || this.manga != null)
    }

}