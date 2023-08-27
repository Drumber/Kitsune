package io.github.drumber.kitsune.domain.database

import androidx.lifecycle.LiveData
import androidx.paging.PagingSource
import androidx.room.*
import io.github.drumber.kitsune.domain.model.database.LocalLibraryEntry
import io.github.drumber.kitsune.domain.model.infrastructure.library.LibraryStatus

@Dao
interface LibraryEntryDao {

    companion object {
        /** Order by status orderId (see [io.github.drumber.kitsune.domain.model.infrastructure.library.LibraryStatus]) and update time */
        const val ORDER_BY_STATUS = "ORDER BY status, DATETIME(updatedAt) DESC"
    }


    /* ===================
     * All Library Status
     * =================== */

    @Transaction
    @Query("SELECT * FROM library_entries $ORDER_BY_STATUS")
    fun getAllLibraryEntry(): PagingSource<Int, LocalLibraryEntry>

    @Transaction
    @Query("SELECT * FROM library_entries WHERE anime_id IS NOT NULL $ORDER_BY_STATUS")
    fun getAnimeLibraryEntry(): PagingSource<Int, LocalLibraryEntry>

    @Transaction
    @Query("SELECT * FROM library_entries WHERE manga_id IS NOT NULL $ORDER_BY_STATUS")
    fun getMangaLibraryEntry(): PagingSource<Int, LocalLibraryEntry>


    /* ===================
     * Filtered by Status
     * =================== */

    @Transaction
    @Query("SELECT * FROM library_entries WHERE status IN (:status) $ORDER_BY_STATUS")
    fun getAllLibraryEntry(status: List<LibraryStatus>): PagingSource<Int, LocalLibraryEntry>

    @Transaction
    @Query("SELECT * FROM library_entries WHERE status IN (:status) AND anime_id IS NOT NULL $ORDER_BY_STATUS")
    fun getAnimeLibraryEntry(status: List<LibraryStatus>): PagingSource<Int, LocalLibraryEntry>

    @Transaction
    @Query("SELECT * FROM library_entries WHERE status IN (:status) AND manga_id IS NOT NULL $ORDER_BY_STATUS")
    fun getMangaLibraryEntry(status: List<LibraryStatus>): PagingSource<Int, LocalLibraryEntry>


    /* ===================
     * Non Paging Queries
     * =================== */

    @Transaction
    @Query("SELECT * FROM library_entries WHERE status IN (:status) $ORDER_BY_STATUS")
    suspend fun getAllLibraryEntryByStatus(status: List<LibraryStatus>): List<LocalLibraryEntry>

    @Transaction
    @Query("SELECT * FROM library_entries WHERE status IN (:status) AND anime_id IS NOT NULL $ORDER_BY_STATUS")
    suspend fun getAnimeLibraryEntryByStatus(status: List<LibraryStatus>): List<LocalLibraryEntry>

    @Transaction
    @Query("SELECT * FROM library_entries WHERE status IN (:status) AND manga_id IS NOT NULL $ORDER_BY_STATUS")
    suspend fun getMangaLibraryEntryByStatus(status: List<LibraryStatus>): List<LocalLibraryEntry>

    @Transaction
    @Query("SELECT * FROM library_entries WHERE anime_id = :mediaId OR manga_id = :mediaId")
    suspend fun getLibraryEntryFromMedia(mediaId: String): LocalLibraryEntry?

    @Transaction
    @Query("SELECT * FROM library_entries WHERE anime_id = :mediaId OR manga_id = :mediaId")
    fun getLibraryEntryFromMediaLiveData(mediaId: String): LiveData<LocalLibraryEntry?>

    @Transaction
    @Query("SELECT * FROM library_entries WHERE id = :id")
    suspend fun getLibraryEntry(id: String): LocalLibraryEntry?

    @Transaction
    @Query("SELECT * FROM library_entries WHERE id = :id")
    fun getLibraryEntryAsLiveData(id: String): LiveData<LocalLibraryEntry?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(libraryEntry: List<LocalLibraryEntry>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSingle(libraryEntry: LocalLibraryEntry)

    @Update
    suspend fun updateLibraryEntry(libraryEntry: LocalLibraryEntry)

    @Delete
    suspend fun delete(libraryEntry: LocalLibraryEntry)

    @Delete
    suspend fun delete(libraryEntries: List<LocalLibraryEntry>)

    @Query("DELETE FROM library_entries")
    suspend fun clearLibraryEntries()

}