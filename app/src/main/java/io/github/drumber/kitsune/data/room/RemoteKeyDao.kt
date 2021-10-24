package io.github.drumber.kitsune.data.room

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import io.github.drumber.kitsune.data.model.RemoteKey

@Dao
interface RemoteKeyDao {

    @Query("SELECT * FROM remote_keys WHERE queryOptions = :queryOptions")
    fun remoteKeyByQuery(queryOptions: String): RemoteKey

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(key: RemoteKey)

    @Query("DELETE FROM remote_keys WHERE queryOptions = :queryOptions")
    suspend fun deleteByQuery(queryOptions: String)

}