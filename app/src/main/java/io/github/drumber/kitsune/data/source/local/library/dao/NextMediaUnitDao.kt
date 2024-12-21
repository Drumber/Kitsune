package io.github.drumber.kitsune.data.source.local.library.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Query
import androidx.room.Upsert
import io.github.drumber.kitsune.data.source.local.library.model.LocalNextMediaUnit

@Dao
interface NextMediaUnitDao {

    @Query("SELECT * FROM next_media_units WHERE id = :id")
    suspend fun getSingle(id: String): LocalNextMediaUnit?

    @Upsert
    suspend fun insertSingle(nextMediaUnits: LocalNextMediaUnit)

    @Delete
    suspend fun deleteSingle(nextMediaUnit: LocalNextMediaUnit)
}