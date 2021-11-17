package io.github.drumber.kitsune.data.room

import androidx.paging.PagingSource
import androidx.room.*
import io.github.drumber.kitsune.data.model.library.LibraryEntry
import io.github.drumber.kitsune.data.model.library.Status

@Dao
interface LibraryEntryDao {

    @Query("SELECT * FROM library_table")
    fun getAllLibraryEntry(): PagingSource<Int, LibraryEntry>

    @Query("SELECT * FROM library_table WHERE anime_id IS NOT NULL")
    fun getAnimeLibraryEntry(): PagingSource<Int, LibraryEntry>

    @Query("SELECT * FROM library_table WHERE manga_id IS NOT NULL")
    fun getMangaLibraryEntry(): PagingSource<Int, LibraryEntry>

    @Query("SELECT * FROM library_table WHERE status IN (:status)")
    fun getAllLibraryEntry(status: List<Status>): PagingSource<Int, LibraryEntry>

    @Query("SELECT * FROM library_table WHERE status IN (:status) AND anime_id IS NOT NULL")
    fun getAnimeLibraryEntry(status: List<Status>): PagingSource<Int, LibraryEntry>

    @Query("SELECT * FROM library_table WHERE status IN (:status) AND manga_id IS NOT NULL")
    fun getMangaLibraryEntry(status: List<Status>): PagingSource<Int, LibraryEntry>

    @Query("SELECT * FROM library_table WHERE anime_id = :resourceId OR manga_id = :resourceId")
    suspend fun getLibraryEntryFromResource(resourceId: String): LibraryEntry?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(libraryEntry: List<LibraryEntry>)

    @Update
    suspend fun updateLibraryEntry(libraryEntry: LibraryEntry)

    @Delete
    suspend fun delete(libraryEntry: LibraryEntry)

    @Query("DELETE FROM library_table")
    suspend fun clearLibraryEntries()

}