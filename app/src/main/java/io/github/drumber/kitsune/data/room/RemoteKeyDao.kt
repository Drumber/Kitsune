package io.github.drumber.kitsune.data.room

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import io.github.drumber.kitsune.data.model.RemoteKey

@Dao
interface RemoteKeyDao {

    @Query("SELECT * FROM remote_keys WHERE resourceId = :resourceId")
    suspend fun remoteKeyByResourceId(resourceId: String): RemoteKey

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertALl(key: List<RemoteKey>)

    @Query("DELETE FROM remote_keys WHERE resourceId = :resourceId")
    suspend fun deleteByResourceId(resourceId: String)

    @Query("DELETE FROM remote_keys")
    suspend fun clearRemoteKeys()

}