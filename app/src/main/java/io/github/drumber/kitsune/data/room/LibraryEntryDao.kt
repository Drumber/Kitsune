package io.github.drumber.kitsune.data.room

import androidx.paging.PagingSource
import androidx.room.*
import io.github.drumber.kitsune.data.model.library.LibraryEntry

@Dao
interface LibraryEntryDao {

    @Query("SELECT * FROM library_table")
    fun getLibraryEntry(): PagingSource<Int, LibraryEntry>

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