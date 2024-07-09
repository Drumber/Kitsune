package io.github.drumber.kitsune.data.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.map
import androidx.paging.ExperimentalPagingApi
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.map
import io.github.drumber.kitsune.constants.Repository
import io.github.drumber.kitsune.data.common.Filter
import io.github.drumber.kitsune.data.common.exception.NoDataException
import io.github.drumber.kitsune.data.common.exception.NotFoundException
import io.github.drumber.kitsune.data.common.library.LibraryEntryKind
import io.github.drumber.kitsune.data.mapper.LibraryMapper.toLibraryEntry
import io.github.drumber.kitsune.data.mapper.LibraryMapper.toLibraryEntryModification
import io.github.drumber.kitsune.data.mapper.LibraryMapper.toLocalLibraryEntry
import io.github.drumber.kitsune.data.mapper.LibraryMapper.toLocalLibraryEntryModification
import io.github.drumber.kitsune.data.mapper.LibraryMapper.toLocalLibraryModificationState
import io.github.drumber.kitsune.data.mapper.LibraryMapper.toLocalLibraryStatus
import io.github.drumber.kitsune.data.mapper.LibraryMapper.toNetworkLibraryStatus
import io.github.drumber.kitsune.data.presentation.model.library.LibraryEntry
import io.github.drumber.kitsune.data.presentation.model.library.LibraryEntryFilter
import io.github.drumber.kitsune.data.presentation.model.library.LibraryEntryModification
import io.github.drumber.kitsune.data.presentation.model.library.LibraryEntryWithModification
import io.github.drumber.kitsune.data.presentation.model.library.LibraryModificationState
import io.github.drumber.kitsune.data.presentation.model.library.LibraryStatus
import io.github.drumber.kitsune.data.presentation.model.media.Anime
import io.github.drumber.kitsune.data.presentation.model.media.Manga
import io.github.drumber.kitsune.data.presentation.model.media.Media
import io.github.drumber.kitsune.data.source.local.library.LibraryLocalDataSource
import io.github.drumber.kitsune.data.source.local.library.model.LocalLibraryEntry
import io.github.drumber.kitsune.data.source.local.library.model.LocalLibraryEntryModification
import io.github.drumber.kitsune.data.source.network.library.LibraryEntryPagingDataSource
import io.github.drumber.kitsune.data.source.network.library.LibraryNetworkDataSource
import io.github.drumber.kitsune.data.source.network.library.model.NetworkLibraryEntry
import io.github.drumber.kitsune.data.utils.InvalidatingPagingSourceFactory
import io.github.drumber.kitsune.util.logD
import io.github.drumber.kitsune.util.parseUtcDate
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import retrofit2.HttpException

class LibraryRepository(
    private val remoteLibraryDataSource: LibraryNetworkDataSource,
    private val localLibraryDataSource: LibraryLocalDataSource,
    private val coroutineScope: CoroutineScope
) {

    private val filterForFullLibraryEntry
        get() = Filter().include("anime", "manga")

    suspend fun addNewLibraryEntry(
        userId: String,
        media: Media,
        status: LibraryStatus
    ): LibraryEntry? {
        val newLibraryEntry = NetworkLibraryEntry.new(
            userId,
            media.mediaType,
            media.id,
            status.toNetworkLibraryStatus()
        )

        return coroutineScope.async {
            val libraryEntry = remoteLibraryDataSource.postLibraryEntry(
                newLibraryEntry,
                filterForFullLibraryEntry
            )

            if (libraryEntry != null) {
                localLibraryDataSource.insertLibraryEntry(libraryEntry.toLocalLibraryEntry())
            }
            libraryEntry?.toLibraryEntry()
        }.await()
    }

    suspend fun removeLibraryEntry(libraryEntryId: String) {
        remoteLibraryDataSource.deleteLibraryEntry(libraryEntryId)
        localLibraryDataSource.deleteLibraryEntryAndAnyModification(libraryEntryId)
    }

    /**
     * Check if library entry was deleted on the server. If so, remove it from local database.
     */
    suspend fun mayRemoveLibraryEntryLocally(libraryEntryId: String) {
        if (!doesLibraryEntryExist(libraryEntryId)) {
            localLibraryDataSource.deleteLibraryEntryAndAnyModification(libraryEntryId)
        }
    }

    private suspend fun doesLibraryEntryExist(libraryEntryId: String): Boolean {
        return try {
            remoteLibraryDataSource.getLibraryEntry(
                libraryEntryId,
                Filter().fields("libraryEntries", "id")
            ) != null
        } catch (e: HttpException) {
            if (e.code() == 404)
                return false
            throw e
        }
    }

    suspend fun updateLibraryEntry(
        libraryEntryModification: LibraryEntryModification
    ): LibraryEntry {
        val modification =
            libraryEntryModification.copy(state = LibraryModificationState.SYNCHRONIZING)

        return try {
            coroutineScope.async {
                localLibraryDataSource.insertLibraryEntryModification(modification.toLocalLibraryEntryModification())
            }.await()

            val libraryEntry = pushModificationToService(modification)
            coroutineScope.async {
                if (isLibraryEntryNotOlderThanInDatabase(libraryEntry.toLocalLibraryEntry())) {
                    localLibraryDataSource.updateLibraryEntryAndDeleteModification(
                        libraryEntry.toLocalLibraryEntry(),
                        modification.toLocalLibraryEntryModification()
                    )
                }
            }.await()
            libraryEntry.toLibraryEntry()
        } catch (e: NotFoundException) {
            localLibraryDataSource.deleteLibraryEntryAndAnyModification(modification.id)
            throw e
        } catch (e: Exception) {
            insertLocalModificationOrDeleteIfSameAsLibraryEntry(
                modification.copy(state = LibraryModificationState.NOT_SYNCHRONIZED)
                    .toLocalLibraryEntryModification()
            )
            throw e
        }
    }

    suspend fun fetchAndStoreLibraryEntryForMedia(userId: String, media: Media): LibraryEntry? {
        val requestFilter = filterForFullLibraryEntry.copy()
            .filter("user_id", userId)
        when (media) {
            is Anime -> requestFilter.filter("anime_id", media.id)
            is Manga -> requestFilter.filter("manga_id", media.id)
        }

        val filter = LibraryEntryFilter(
            kind = LibraryEntryKind.All,
            libraryStatus = emptyList(),
            initialFilter = requestFilter
        )
        return fetchAndStoreLibraryEntriesForFilter(filter)?.firstOrNull()
    }

    suspend fun fetchAndStoreLibraryEntriesForFilter(filter: LibraryEntryFilter): List<LibraryEntry>? {
        val libraryEntries = remoteLibraryDataSource.getAllLibraryEntries(filter.buildFilter()).data
        if (libraryEntries != null) {
            localLibraryDataSource.insertAllLibraryEntries(libraryEntries.map { it.toLocalLibraryEntry() })
        }
        return libraryEntries?.map { it.toLibraryEntry() }
    }

    suspend fun fetchAllLibraryEntries(filter: Filter): List<LibraryEntry>? {
        return remoteLibraryDataSource.getAllLibraryEntries(filter).data?.map { it.toLibraryEntry() }
    }

    suspend fun fetchLibraryEntry(id: String, filter: Filter): LibraryEntry? {
        return remoteLibraryDataSource.getLibraryEntry(id, filter)?.toLibraryEntry()
    }

    suspend fun getLibraryEntryFromDatabase(id: String): LibraryEntry? {
        return localLibraryDataSource.getLibraryEntry(id)?.toLibraryEntry()
    }

    suspend fun getLibraryEntryFromMedia(mediaId: String): LibraryEntry? {
        return localLibraryDataSource.getLibraryEntryFromMedia(mediaId)?.toLibraryEntry()
    }

    suspend fun getLibraryEntriesWithModificationsByStatus(status: List<LibraryStatus>): List<LibraryEntryWithModification> {
        return localLibraryDataSource.getLibraryEntriesWithModificationsByStatus(
            status.map { it.toLocalLibraryStatus() }
        ).map {
            LibraryEntryWithModification(
                libraryEntry = it.libraryEntry.toLibraryEntry(),
                modification = it.libraryEntryModification?.toLibraryEntryModification()
            )
        }
    }

    fun getLibraryEntryWithModificationFromMediaAsLiveData(mediaId: String): LiveData<LibraryEntryWithModification?> {
        return localLibraryDataSource.getLibraryEntryWithModificationFromMediaAsLiveData(mediaId)
            .map { entry ->
                entry?.let {
                    LibraryEntryWithModification(
                        libraryEntry = it.libraryEntry.toLibraryEntry(),
                        modification = it.libraryEntryModification?.toLibraryEntryModification()
                    )
                }
            }
    }

    //********************************************************************************************//
    // Library modifications related methods
    //********************************************************************************************//

    suspend fun getLibraryEntryModification(id: String): LibraryEntryModification? {
        return localLibraryDataSource.getLibraryEntryModification(id)?.toLibraryEntryModification()
    }

    suspend fun getAllLibraryEntryModifications(): List<LibraryEntryModification> {
        return localLibraryDataSource.getAllLocalLibraryModifications()
            .map { it.toLibraryEntryModification() }
    }

    fun getLibraryEntryModificationsAsFlow(): Flow<List<LibraryEntryModification>> {
        return localLibraryDataSource.getAllLibraryEntryModificationsAsFlow()
            .map { modifications ->
                modifications.map { it.toLibraryEntryModification() }
            }
    }

    fun getLibraryEntryModificationsByStateAsLiveData(state: LibraryModificationState): LiveData<List<LibraryEntryModification>> {
        return localLibraryDataSource
            .getLibraryEntryModificationsByStateAsLiveData(state.toLocalLibraryModificationState())
            .map { modifications ->
                modifications.map { it.toLibraryEntryModification() }
            }
    }

    private suspend fun pushModificationToService(
        modification: LibraryEntryModification
    ): NetworkLibraryEntry {
        val updatedLibraryEntry = NetworkLibraryEntry.update(
            id = modification.id,
            startedAt = modification.startedAt,
            finishedAt = modification.finishedAt,
            status = modification.status?.toNetworkLibraryStatus(),
            progress = modification.progress,
            reconsumeCount = modification.reconsumeCount,
            volumesOwned = modification.volumesOwned,
            ratingTwenty = modification.ratingTwenty,
            notes = modification.notes,
            isPrivate = modification.privateEntry,
        )

        try {
            val libraryEntry = remoteLibraryDataSource.updateLibraryEntry(
                modification.id,
                updatedLibraryEntry,
                filterForFullLibraryEntry
            )
                ?: throw NoDataException("Received library entry for ID '${modification.id}' is 'null'.")
            return libraryEntry
        } catch (e: HttpException) {
            if (e.code() == 404) {
                throw NotFoundException(
                    "Library entry with ID '${modification.id}' does not exist.",
                    e
                )
            }
            throw e
        }
    }

    private suspend fun insertLocalModificationOrDeleteIfSameAsLibraryEntry(
        libraryEntryModification: LocalLibraryEntryModification
    ) {
        val modificationInDb =
            localLibraryDataSource.getLibraryEntryModification(libraryEntryModification.id)
        if (modificationInDb != null && modificationInDb.createTime > libraryEntryModification.createTime) {
            logD("Modification in database is newer than the one being inserted. Ignoring $libraryEntryModification")
            return
        }

        val libraryEntry = localLibraryDataSource.getLibraryEntry(libraryEntryModification.id)
        if (libraryEntry != null && libraryEntryModification.isEqualToLibraryEntry(libraryEntry)) {
            localLibraryDataSource.deleteLibraryEntryModification(libraryEntryModification)
        } else {
            localLibraryDataSource.insertLibraryEntryModification(libraryEntryModification)
        }
    }

    private suspend fun isLibraryEntryNotOlderThanInDatabase(libraryEntry: LocalLibraryEntry): Boolean {
        return localLibraryDataSource.getLibraryEntry(libraryEntry.id)?.let { dbEntry ->
            val dbUpdatedAt = dbEntry.updatedAt?.parseUtcDate() ?: return true
            val thisUpdatedAt = libraryEntry.updatedAt?.parseUtcDate() ?: return true
            thisUpdatedAt.time >= dbUpdatedAt.time
        } ?: true
    }

    //********************************************************************************************//
    // Paging related methods
    //********************************************************************************************//

    private val invalidatingPagingSourceFactory =
        InvalidatingPagingSourceFactory<Int, LocalLibraryEntry, LibraryEntryFilter> { filter ->
            localLibraryDataSource.getLibraryEntriesByKindAndStatusAsPagingSource(
                kind = filter.kind,
                status = filter.libraryStatus.map { it.toLocalLibraryStatus() }
            )
        }

    @OptIn(ExperimentalPagingApi::class)
    fun libraryEntriesPager(pageSize: Int, filter: LibraryEntryFilter) = Pager(
        config = PagingConfig(
            pageSize = pageSize,
            maxSize = Repository.MAX_CACHED_ITEMS
        ),
        remoteMediator = LibraryEntryRemoteMediator(
            filter.pageSize(pageSize),
            remoteLibraryDataSource,
            localLibraryDataSource
        ),
        pagingSourceFactory = { invalidatingPagingSourceFactory.createPagingSource(filter) }
    ).flow.map { pagingData ->
        pagingData.map { it.toLibraryEntry() }
    }

    fun searchLibraryEntriesPager(pageSize: Int, filter: Filter) = Pager(
        config = PagingConfig(
            pageSize = pageSize,
            maxSize = Repository.MAX_CACHED_ITEMS
        ),
        pagingSourceFactory = {
            LibraryEntryPagingDataSource(
                remoteLibraryDataSource,
                filter.pageLimit(pageSize)
            )
        }
    ).flow.map { pagingData ->
        pagingData.map { it.toLibraryEntry() }
    }

    fun invalidatePagingSources() {
        invalidatingPagingSourceFactory.invalidate()
    }
}