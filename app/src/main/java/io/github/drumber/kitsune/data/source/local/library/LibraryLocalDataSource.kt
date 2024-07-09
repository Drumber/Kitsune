package io.github.drumber.kitsune.data.source.local.library

import androidx.paging.PagingSource
import androidx.room.withTransaction
import io.github.drumber.kitsune.data.common.library.LibraryEntryKind
import io.github.drumber.kitsune.data.source.local.LocalDatabase
import io.github.drumber.kitsune.data.source.local.library.model.LocalLibraryEntry
import io.github.drumber.kitsune.data.source.local.library.model.LocalLibraryEntryModification
import io.github.drumber.kitsune.data.source.local.library.model.LocalLibraryMedia.MediaType
import io.github.drumber.kitsune.data.source.local.library.model.LocalLibraryModificationState
import io.github.drumber.kitsune.data.source.local.library.model.LocalLibraryStatus
import io.github.drumber.kitsune.data.source.local.library.model.RemoteKeyType

class LibraryLocalDataSource(
    private val database: LocalDatabase
) {

    private val libraryEntryDao
        get() = database.libraryEntryDao()
    private val libraryEntryModificationDao
        get() = database.libraryEntryModificationDao()
    private val libraryEntryWithModificationDao
        get() = database.libraryEntryWithModificationDao()
    private val remoteKeyDao
        get() = database.remoteKeyDao()

    //********************************************************************************************//
    // LibraryEntry related methods
    //********************************************************************************************//

    suspend fun getLibraryEntry(id: String) = libraryEntryDao.getLibraryEntry(id)

    suspend fun getLibraryEntryFromMedia(mediaId: String) =
        libraryEntryDao.getLibraryEntryFromMedia(mediaId)

    suspend fun getLibraryEntriesWithModificationsByStatus(status: List<LocalLibraryStatus>) =
        libraryEntryWithModificationDao.getLibraryEntriesWithModificationByStatus(status)

    fun getLibraryEntryWithModificationFromMediaAsLiveData(mediaId: String) =
        libraryEntryWithModificationDao.getLibraryEntryWithModificationFromMediaAsLiveData(mediaId)

    suspend fun insertAllLibraryEntries(libraryEntries: List<LocalLibraryEntry>) {
        database.withTransaction {
            libraryEntries.forEach {
                insertLibraryEntry(it)
            }
        }
    }

    suspend fun insertLibraryEntry(libraryEntry: LocalLibraryEntry) {
        libraryEntry.verifyIsValidLibraryEntry()
        insertLibraryEntryIfUpdatedAtIsNewer(libraryEntry)
    }

    suspend fun insertLibraryEntryIfUpdatedAtIsNewer(libraryEntry: LocalLibraryEntry): Boolean {
        libraryEntry.verifyIsValidLibraryEntry()
        if (libraryEntry.updatedAt.isNullOrBlank()) {
            insertLibraryEntry(libraryEntry)
            return true
        }
        return database.withTransaction {
            val hasNewerEntry = libraryEntryDao.hasLibraryEntryWhereUpdatedAtIsAfter(
                libraryEntry.id,
                libraryEntry.updatedAt
            )
            if (!hasNewerEntry) {
                libraryEntryDao.insertSingle(libraryEntry)
                true
            } else {
                // do not overwrite more up-to-date library entry
                false
            }
        }
    }

    suspend fun getLibraryEntriesByKindAndStatus(
        kind: LibraryEntryKind,
        status: List<LocalLibraryStatus>
    ): List<LocalLibraryEntry> {
        val hasStatus = status.isNotEmpty()
        return with(libraryEntryDao) {
            when {
                kind == LibraryEntryKind.Anime && hasStatus -> getAllLibraryEntriesByTypeAndStatus(
                    MediaType.Anime,
                    status
                )

                kind == LibraryEntryKind.Anime && !hasStatus -> getAllLibraryEntriesByType(
                    MediaType.Anime
                )

                kind == LibraryEntryKind.Manga && hasStatus -> getAllLibraryEntriesByTypeAndStatus(
                    MediaType.Manga,
                    status
                )

                kind == LibraryEntryKind.Manga && !hasStatus -> getAllLibraryEntriesByType(
                    MediaType.Manga
                )

                kind == LibraryEntryKind.All && hasStatus -> getAllLibraryEntriesByStatus(
                    status
                )

                else -> getAllLibraryEntries()
            }
        }
    }

    fun getLibraryEntriesByKindAndStatusAsPagingSource(
        kind: LibraryEntryKind,
        status: List<LocalLibraryStatus>
    ): PagingSource<Int, LocalLibraryEntry> {
        val hasStatus = status.isNotEmpty()
        return with(libraryEntryDao) {
            when {
                kind == LibraryEntryKind.Anime && hasStatus -> allLibraryEntriesByTypeAndStatusPagingSource(
                    MediaType.Anime,
                    status
                )

                kind == LibraryEntryKind.Anime && !hasStatus -> allLibraryEntriesByTypePagingSource(
                    MediaType.Anime
                )

                kind == LibraryEntryKind.Manga && hasStatus -> allLibraryEntriesByTypeAndStatusPagingSource(
                    MediaType.Manga,
                    status
                )

                kind == LibraryEntryKind.Manga && !hasStatus -> allLibraryEntriesByTypePagingSource(
                    MediaType.Manga
                )

                kind == LibraryEntryKind.All && hasStatus -> allLibraryEntriesByStatusPagingSource(
                    status
                )

                else -> allLibraryEntriesPagingSource()
            }
        }
    }

    //********************************************************************************************//
    // LibraryEntryModification related methods
    //********************************************************************************************//

    suspend fun getLibraryEntryModification(id: String) =
        libraryEntryModificationDao.getLibraryEntryModification(id)

    suspend fun getAllLocalLibraryModifications(): List<LocalLibraryEntryModification> {
        return libraryEntryModificationDao.getAllLibraryEntryModifications()
    }

    fun getAllLibraryEntryModificationsAsFlow() =
        libraryEntryModificationDao.getAllLibraryEntryModificationsAsFlow()

    fun getLibraryEntryModificationsByStateAsLiveData(state: LocalLibraryModificationState) =
        libraryEntryModificationDao.getLibraryEntryModificationsByStateAsLiveData(state)

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

    //********************************************************************************************//
    // RemoteKey related methods
    //********************************************************************************************//

    suspend fun getRemoteKeyByResourceId(resourceId: String, remoteKeyType: RemoteKeyType) =
        remoteKeyDao.getRemoteKeyByResourceId(resourceId, remoteKeyType)

    //********************************************************************************************//
    // Utilities
    //********************************************************************************************//

    suspend fun <R> runDatabaseTransaction(block: suspend LocalDatabase.() -> R) =
        database.withTransaction {
            block(database)
        }

    /**
     * Verifies that the library entry has an ID and contains a media object.
     *
     * @throws IllegalArgumentException if the library entry is not valid
     */
    private fun LocalLibraryEntry.verifyIsValidLibraryEntry() {
        requireNotNull(this.id)
        requireNotNull(this.media)
    }
}