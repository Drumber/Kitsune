package io.github.drumber.kitsune.data.source.local.library

import androidx.paging.PagingSource
import androidx.room.withTransaction
import io.github.drumber.kitsune.data.common.model.library.LibraryEntryMediaType
import io.github.drumber.kitsune.data.common.model.library.LibraryFilterOptions
import io.github.drumber.kitsune.data.common.model.library.LibraryFilterOptions.SortBy
import io.github.drumber.kitsune.data.common.model.library.LibraryFilterOptions.SortDirection
import io.github.drumber.kitsune.data.common.mapper.toMediaType
import io.github.drumber.kitsune.data.source.local.LocalDatabase
import io.github.drumber.kitsune.data.source.local.library.model.LocalLibraryEntry
import io.github.drumber.kitsune.data.source.local.library.model.LocalLibraryEntryModification
import io.github.drumber.kitsune.data.source.local.library.model.LocalLibraryEntryWithModificationAndNextMediaUnit
import io.github.drumber.kitsune.data.source.local.library.model.LocalLibraryMedia.MediaType
import io.github.drumber.kitsune.data.source.local.library.model.LocalLibraryModificationState
import io.github.drumber.kitsune.data.source.local.library.model.LocalLibraryStatus
import io.github.drumber.kitsune.data.source.local.library.model.LocalNextMediaUnit
import io.github.drumber.kitsune.data.source.local.library.model.RemoteKeyEntity
import io.github.drumber.kitsune.data.source.local.mapper.toLocalLibraryFilterOptions
import io.github.drumber.kitsune.data.source.local.mapper.toLocalLibraryStatus

class LibraryLocalDataSource(
    private val database: LocalDatabase
) {

    private val libraryEntryDao
        get() = database.libraryEntryDao()
    private val libraryEntryModificationDao
        get() = database.libraryEntryModificationDao()
    private val libraryEntryWithModificationDao
        get() = database.libraryEntryWithModificationDao()
    private val libraryEntryWithModificationAndNextMediaUnitDao
        get() = database.libraryEntryWithModificationAndNextMediaUnitDao()
    private val nextMediaUnitDao
        get() = database.nextMediaUnitDao()
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

    fun getLibraryEntriesWithModificationsByStatusAsFlow(status: List<LocalLibraryStatus>) =
        libraryEntryWithModificationDao.getLibraryEntriesWithModificationByStatusAsFlow(status)

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
        return database.withTransaction {
            val hasNewerEntry =
                !libraryEntry.updatedAt.isNullOrBlank() && libraryEntryDao.hasLibraryEntryWhereUpdatedAtIsAfter(
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
        kind: LibraryEntryMediaType,
        status: List<LocalLibraryStatus>
    ): List<LocalLibraryEntry> {
        val hasStatus = status.isNotEmpty()
        return with(libraryEntryDao) {
            when {
                kind == LibraryEntryMediaType.Anime && hasStatus -> getAllLibraryEntriesByTypeAndStatus(
                    MediaType.Anime,
                    status
                )

                kind == LibraryEntryMediaType.Anime && !hasStatus -> getAllLibraryEntriesByType(
                    MediaType.Anime
                )

                kind == LibraryEntryMediaType.Manga && hasStatus -> getAllLibraryEntriesByTypeAndStatus(
                    MediaType.Manga,
                    status
                )

                kind == LibraryEntryMediaType.Manga && !hasStatus -> getAllLibraryEntriesByType(
                    MediaType.Manga
                )

                kind == LibraryEntryMediaType.All && hasStatus -> getAllLibraryEntriesByStatus(
                    status
                )

                else -> getAllLibraryEntries()
            }
        }
    }

    fun getLibraryEntriesByKindAndStatusAsPagingSource(
        kind: LibraryEntryMediaType,
        status: List<LocalLibraryStatus>
    ): PagingSource<Int, LocalLibraryEntry> {
        val hasStatus = status.isNotEmpty()
        return with(libraryEntryDao) {
            when {
                kind == LibraryEntryMediaType.Anime && hasStatus -> allLibraryEntriesByTypeAndStatusPagingSource(
                    MediaType.Anime,
                    status
                )

                kind == LibraryEntryMediaType.Anime && !hasStatus -> allLibraryEntriesByTypePagingSource(
                    MediaType.Anime
                )

                kind == LibraryEntryMediaType.Manga && hasStatus -> allLibraryEntriesByTypeAndStatusPagingSource(
                    MediaType.Manga,
                    status
                )

                kind == LibraryEntryMediaType.Manga && !hasStatus -> allLibraryEntriesByTypePagingSource(
                    MediaType.Manga
                )

                kind == LibraryEntryMediaType.All && hasStatus -> allLibraryEntriesByStatusPagingSource(
                    status
                )

                else -> allLibraryEntriesPagingSource()
            }
        }
    }

    fun getLibraryEntriesWithModificationAndNextUnitAsPagingSource(
        filter: LibraryFilterOptions
    ): PagingSource<Int, LocalLibraryEntryWithModificationAndNextMediaUnit> {
        return libraryEntryWithModificationAndNextMediaUnitDao.getByFilterAsPagingSource(
            filter.status?.map { it.toLocalLibraryStatus() } ?: LocalLibraryStatus.entries,
            filter.mediaType.toMediaType(),
            filter.sortBy ?: SortBy.UPDATED_AT,
            filter.sortDirection ?: SortDirection.DESC
        )
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
    // NextMediaUnit related methods
    //********************************************************************************************//

    suspend fun insertNextMediaUnit(nextMediaUnit: LocalNextMediaUnit) {
        nextMediaUnitDao.insertSingle(nextMediaUnit)
    }

    //********************************************************************************************//
    // RemoteKey related methods
    //********************************************************************************************//

    suspend fun getRemoteKeyByResourceId(resourceId: String, filter: LibraryFilterOptions) =
        remoteKeyDao.getRemoteKeyByResourceId(
            resourceId,
            filter.toLocalLibraryFilterOptions().serialize()
        )

    suspend fun deleteRemoteKey(remoteKey: RemoteKeyEntity) =
        remoteKeyDao.deleteSingle(remoteKey)

    suspend fun deleteRemoteKeyByResourceId(resourceId: String, filter: LibraryFilterOptions) =
        remoteKeyDao.deleteByResourceId(
            resourceId,
            filter.toLocalLibraryFilterOptions().serialize()
        )

    suspend fun deleteAllRemoteKeysByResourceId(
        resourceIds: List<String>,
        filter: LibraryFilterOptions
    ) = remoteKeyDao.deleteAllByResourceId(
        resourceIds,
        filter.toLocalLibraryFilterOptions().serialize()
    )

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