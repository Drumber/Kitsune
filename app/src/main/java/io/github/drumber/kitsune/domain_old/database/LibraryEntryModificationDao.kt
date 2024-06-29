package io.github.drumber.kitsune.domain_old.database

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import io.github.drumber.kitsune.domain_old.model.database.LocalLibraryEntryModification
import io.github.drumber.kitsune.domain_old.model.database.LocalLibraryModificationState

@Dao
interface LibraryEntryModificationDao {

    @Query("SELECT * FROM library_entries_modifications")
    fun getAllLibraryEntryModificationsLiveData(): LiveData<List<LocalLibraryEntryModification>>

    @Query("SELECT * FROM library_entries_modifications WHERE state = :state")
    fun getLibraryEntryModificationsWithStateLiveData(state: LocalLibraryModificationState): LiveData<List<LocalLibraryEntryModification>>

    @Query("SELECT * FROM library_entries_modifications")
    suspend fun getAllLibraryEntryModifications(): List<LocalLibraryEntryModification>

    @Query("SELECT * FROM library_entries_modifications WHERE id = :id")
    suspend fun getLibraryEntryModification(id: String): LocalLibraryEntryModification?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSingle(libraryEntryModification: LocalLibraryEntryModification)

    @Update
    suspend fun updateSingle(libraryEntryModification: LocalLibraryEntryModification)

    @Delete
    suspend fun deleteSingle(libraryEntryModification: LocalLibraryEntryModification)

    @Query("DELETE FROM library_entries_modifications WHERE id = :id")
    suspend fun deleteSingleById(id: String)

    @Query("DELETE FROM library_entries_modifications WHERE id = :id AND createTime = :createTime")
    suspend fun deleteSingleMatchingCreateTime(id: String, createTime: Long)

    @Query("DELETE FROM library_entries_modifications")
    suspend fun clearAll()

}