package io.github.drumber.kitsune.data.source.local.library.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Query
import androidx.room.Update
import androidx.room.Upsert
import io.github.drumber.kitsune.data.source.local.library.model.LocalLibraryEntryModification
import io.github.drumber.kitsune.data.source.local.library.model.LocalLibraryModificationState
import kotlinx.coroutines.flow.Flow

@Dao
interface LibraryEntryModificationDao {

    @Query("SELECT * FROM library_entries_modifications")
    fun getAllLibraryEntryModificationsAsFlow(): Flow<List<LocalLibraryEntryModification>>

    @Query("SELECT * FROM library_entries_modifications WHERE state = :state")
    fun getLibraryEntryModificationsByStateAsLiveData(state: LocalLibraryModificationState): LiveData<List<LocalLibraryEntryModification>>

    @Query("SELECT * FROM library_entries_modifications")
    suspend fun getAllLibraryEntryModifications(): List<LocalLibraryEntryModification>

    @Query("SELECT * FROM library_entries_modifications WHERE id = :id")
    suspend fun getLibraryEntryModification(id: String): LocalLibraryEntryModification?

    @Upsert
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