package io.github.drumber.kitsune.data.source.local.library.dao

import androidx.lifecycle.LiveData
import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import io.github.drumber.kitsune.data.source.local.library.model.LocalLibraryEntry
import io.github.drumber.kitsune.data.source.local.library.model.LocalLibraryMedia
import io.github.drumber.kitsune.data.source.local.library.model.LocalLibraryStatus

@Dao
interface LibraryEntryDao {

    companion object {
        /** Order by status orderId (see [LocalLibraryStatus]) and update time */
        const val ORDER_BY_STATUS = "ORDER BY status, DATETIME(updatedAt) DESC"
    }


    /* ===================
     * All Library Status
     * =================== */

    @Query("SELECT * FROM library_entries $ORDER_BY_STATUS")
    fun allLibraryEntriesPagingSource(): PagingSource<Int, LocalLibraryEntry>

    @Query("SELECT * FROM library_entries WHERE media_type = :type $ORDER_BY_STATUS")
    fun allLibraryEntriesByTypePagingSource(type: LocalLibraryMedia.MediaType): PagingSource<Int, LocalLibraryEntry>


    /* ===================
     * Filtered by Status
     * =================== */

    @Query("SELECT * FROM library_entries WHERE status IN (:status) $ORDER_BY_STATUS")
    fun allLibraryEntriesByStatusPagingSource(status: List<LocalLibraryStatus>): PagingSource<Int, LocalLibraryEntry>

    @Query("SELECT * FROM library_entries WHERE status IN (:status) AND media_type = :type $ORDER_BY_STATUS")
    fun allLibraryEntriesByTypeAndStatusPagingSource(type: LocalLibraryMedia.MediaType, status: List<LocalLibraryStatus>): PagingSource<Int, LocalLibraryEntry>


    /* ===================
     * Non Paging Queries
     * =================== */

    @Query("SELECT * FROM library_entries WHERE status IN (:status) $ORDER_BY_STATUS")
    suspend fun getAllLibraryEntriesByStatus(status: List<LocalLibraryStatus>): List<LocalLibraryEntry>

    @Query("SELECT * FROM library_entries WHERE status IN (:status) $ORDER_BY_STATUS")
    suspend fun getAllLibraryEntriesByTypeAndStatus(type: LocalLibraryMedia.MediaType, status: List<LocalLibraryStatus>): List<LocalLibraryEntry>

    @Query("SELECT * FROM library_entries WHERE media_id = :mediaId")
    suspend fun getLibraryEntryFromMedia(mediaId: String): LocalLibraryEntry?

    @Query("SELECT * FROM library_entries WHERE media_id = :mediaId")
    fun getLibraryEntryFromMediaAsLiveData(mediaId: String): LiveData<LocalLibraryEntry?>

    @Query("SELECT * FROM library_entries WHERE id = :id")
    suspend fun getLibraryEntry(id: String): LocalLibraryEntry?

    @Query("SELECT * FROM library_entries WHERE id = :id")
    fun getLibraryEntryAsLiveData(id: String): LiveData<LocalLibraryEntry?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(libraryEntry: List<LocalLibraryEntry>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSingle(libraryEntry: LocalLibraryEntry)

    @Update
    suspend fun updateSingle(libraryEntry: LocalLibraryEntry)

    @Delete
    suspend fun deleteSingle(libraryEntry: LocalLibraryEntry)

    @Query("DELETE FROM library_entries WHERE id = :id")
    suspend fun deleteSingleById(id: String)

    @Delete
    suspend fun deleteAll(libraryEntries: List<LocalLibraryEntry>)

    @Query("DELETE FROM library_entries")
    suspend fun clearLibraryEntries()

}