package io.github.drumber.kitsune.data.room

import androidx.lifecycle.LiveData
import androidx.room.*
import io.github.drumber.kitsune.data.model.library.LibraryModification

@Dao
interface OfflineLibraryModificationDao {

    @Query("SELECT * FROM offline_library_modification")
    fun getAllOfflineLibraryModificationsLiveData(): LiveData<List<LibraryModification>>

    @Query("SELECT * FROM offline_library_modification")
    suspend fun getAllOfflineLibraryModifications(): List<LibraryModification>

    @Query("SELECT * FROM offline_library_modification WHERE id = :id")
    suspend fun getOfflineLibraryModification(id: String): LibraryModification?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSingle(libraryModification: LibraryModification)

    @Update
    suspend fun updateOfflineLibraryModification(libraryModification: LibraryModification)

    @Delete
    suspend fun deleteOfflineLibraryModification(libraryModification: LibraryModification)

    @Query("DELETE FROM offline_library_modification")
    suspend fun clearOfflineLibraryModifications()

}