package io.github.drumber.kitsune.data.source.local.library.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import io.github.drumber.kitsune.data.source.local.library.model.LocalNextMediaUnit

@Dao
interface NextMediaUnitDao {

    @Query("SELECT * FROM next_media_units WHERE libraryEntryId = :libraryEntryId")
    suspend fun getForLibraryEntry(libraryEntryId: String): List<LocalNextMediaUnit>

    @Query("SELECT * FROM next_media_units WHERE id = :id")
    suspend fun getSingle(id: String): LocalNextMediaUnit?

    @Insert
    suspend fun insertSingle(nextMediaUnits: LocalNextMediaUnit)

    @Delete
    suspend fun deleteSingle(nextMediaUnit: LocalNextMediaUnit)
}