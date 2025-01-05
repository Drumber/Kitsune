package io.github.drumber.kitsune.data.source.local.library.dao

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import io.github.drumber.kitsune.data.common.library.LibraryFilterOptions
import io.github.drumber.kitsune.data.common.library.LibraryFilterOptions.SortDirection
import io.github.drumber.kitsune.data.common.media.MediaType
import io.github.drumber.kitsune.data.source.local.library.dao.LibraryEntryDao.Companion.ORDER_BY_STATUS
import io.github.drumber.kitsune.data.source.local.library.model.LocalLibraryEntryWithModificationAndNextMediaUnit
import io.github.drumber.kitsune.data.source.local.library.model.LocalLibraryStatus
import kotlinx.coroutines.flow.Flow

@Dao
interface LibraryEntryWithModificationAndNextMediaUnitDao {

    companion object {
        private const val BASE_SELECT_WITH_FILTER = """
            SELECT * FROM library_entries WHERE
              status IN (:status) AND
              (:mediaType IS NULL OR UPPER(media_type) = UPPER(:mediaType))
              ORDER BY
                CASE :sortBy
                    WHEN 'STATUS' THEN status
                    WHEN 'STARTED_AT' THEN DATETIME(startedAt)
                    WHEN 'UPDATED_AT' THEN DATETIME(updatedAt)
                    WHEN 'PROGRESS' THEN progress
                    WHEN 'RATING' THEN ratingTwenty
                END
        """

        private const val SELECT_WITH_FILTER_ASC = "$BASE_SELECT_WITH_FILTER ASC, updatedAt ASC"
        private const val SELECT_WITH_FILTER_DESC = "$BASE_SELECT_WITH_FILTER DESC, updatedAt DESC"
    }

    @Transaction
    @Query("SELECT * FROM library_entries WHERE status IN (:status) $ORDER_BY_STATUS")
    suspend fun getByStatus(status: List<LocalLibraryStatus>): List<LocalLibraryEntryWithModificationAndNextMediaUnit>

    @Transaction
    @Query("SELECT * FROM library_entries WHERE status IN (:status) $ORDER_BY_STATUS")
    fun getByStatusAsFlow(status: List<LocalLibraryStatus>): Flow<List<LocalLibraryEntryWithModificationAndNextMediaUnit>>

    @Transaction
    @Query(SELECT_WITH_FILTER_ASC)
    fun getByFilterAsPagingSourceOrderAsc(
        status: List<LocalLibraryStatus>,
        mediaType: String?,
        sortBy: String
    ): PagingSource<Int, LocalLibraryEntryWithModificationAndNextMediaUnit>

    @Transaction
    @Query(SELECT_WITH_FILTER_DESC)
    fun getByFilterAsPagingSourceOrderDesc(
        status: List<LocalLibraryStatus>,
        mediaType: String?,
        sortBy: String
    ): PagingSource<Int, LocalLibraryEntryWithModificationAndNextMediaUnit>

    fun getByFilterAsPagingSource(
        status: List<LocalLibraryStatus>,
        mediaType: MediaType?,
        sortBy: LibraryFilterOptions.SortBy,
        sortDirection: SortDirection
    ): PagingSource<Int, LocalLibraryEntryWithModificationAndNextMediaUnit> {
        return when (sortDirection) {
            SortDirection.ASC -> getByFilterAsPagingSourceOrderAsc(
                status,
                mediaType?.name,
                sortBy.name
            )

            SortDirection.DESC -> getByFilterAsPagingSourceOrderDesc(
                status,
                mediaType?.name,
                sortBy.name
            )
        }
    }
}