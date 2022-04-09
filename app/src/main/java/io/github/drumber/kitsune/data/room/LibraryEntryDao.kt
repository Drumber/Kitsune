package io.github.drumber.kitsune.data.room

import androidx.lifecycle.LiveData
import androidx.paging.PagingSource
import androidx.room.*
import io.github.drumber.kitsune.data.model.library.LibraryEntry
import io.github.drumber.kitsune.data.model.library.Status

@Dao
interface LibraryEntryDao {

    companion object {
        /** Order by ordinal status (see [io.github.drumber.kitsune.data.model.library.Status]) and update time */
        const val ORDER_BY_STATUS = "ORDER BY status, DATETIME(updatedAt) DESC"
    }

    /* ===================
     * All Library Status
     * =================== */

    @Query("SELECT * FROM library_table $ORDER_BY_STATUS")
    fun getAllLibraryEntry(): PagingSource<Int, LibraryEntry>

    @Query("SELECT * FROM library_table WHERE anime_id IS NOT NULL $ORDER_BY_STATUS")
    fun getAnimeLibraryEntry(): PagingSource<Int, LibraryEntry>

    @Query("SELECT * FROM library_table WHERE manga_id IS NOT NULL $ORDER_BY_STATUS")
    fun getMangaLibraryEntry(): PagingSource<Int, LibraryEntry>

    /* ===================
     * Filtered By Status
     * =================== */

    @Query("SELECT * FROM library_table WHERE status IN (:status) $ORDER_BY_STATUS")
    fun getAllLibraryEntry(status: List<Status>): PagingSource<Int, LibraryEntry>

    @Query("SELECT * FROM library_table WHERE status IN (:status) AND anime_id IS NOT NULL $ORDER_BY_STATUS")
    fun getAnimeLibraryEntry(status: List<Status>): PagingSource<Int, LibraryEntry>

    @Query("SELECT * FROM library_table WHERE status IN (:status) AND manga_id IS NOT NULL $ORDER_BY_STATUS")
    fun getMangaLibraryEntry(status: List<Status>): PagingSource<Int, LibraryEntry>

    /* ===================
     * Non Paging Queries
     * =================== */

    @Query("SELECT * FROM library_table WHERE anime_id = :mediaId OR manga_id = :mediaId")
    suspend fun getLibraryEntryFromMedia(mediaId: String): LibraryEntry?

    @Query("SELECT * FROM library_table WHERE id = :id")
    fun getLibraryEntry(id: String): LibraryEntry?

    @Query("SELECT * FROM library_table WHERE id = :id")
    fun getLibraryEntryAsLiveData(id: String): LiveData<LibraryEntry?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(libraryEntry: List<LibraryEntry>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSingle(libraryEntry: LibraryEntry)

    @Update
    suspend fun updateLibraryEntry(libraryEntry: LibraryEntry)

    @Delete
    suspend fun delete(libraryEntry: LibraryEntry)

    @Query("DELETE FROM library_table")
    suspend fun clearLibraryEntries()

}