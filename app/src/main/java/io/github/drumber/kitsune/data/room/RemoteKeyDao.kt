package io.github.drumber.kitsune.data.room

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import io.github.drumber.kitsune.data.model.RemoteKey
import io.github.drumber.kitsune.data.model.RemoteKeyType

@Dao
interface RemoteKeyDao {

    @Query("SELECT * FROM remote_keys WHERE resourceId = :resourceId AND remoteKeyType = :remoteKeyType")
    suspend fun remoteKeyByResourceId(resourceId: String, remoteKeyType: RemoteKeyType): RemoteKey

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertALl(key: List<RemoteKey>)

    @Query("DELETE FROM remote_keys WHERE resourceId = :resourceId AND remoteKeyType = :remoteKeyType")
    suspend fun deleteByResourceId(resourceId: String, remoteKeyType: RemoteKeyType)

    @Query("DELETE FROM remote_keys WHERE remoteKeyType = :remoteKeyType")
    suspend fun clearRemoteKeys(remoteKeyType: RemoteKeyType)

    @Query("DELETE FROM remote_keys")
    suspend fun clearAllRemoteKeys()

}