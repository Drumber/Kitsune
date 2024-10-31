package io.github.drumber.kitsune.data.source.local.library.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import io.github.drumber.kitsune.data.source.local.library.model.RemoteKeyEntity
import io.github.drumber.kitsune.data.source.local.library.model.RemoteKeyType

@Dao
interface RemoteKeyDao {

    @Query("SELECT * FROM remote_keys WHERE resourceId = :resourceId AND remoteKeyType = :remoteKeyType")
    suspend fun getRemoteKeyByResourceId(resourceId: String, remoteKeyType: RemoteKeyType): RemoteKeyEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertALl(key: List<RemoteKeyEntity>)

    @Query("DELETE FROM remote_keys WHERE resourceId = :resourceId AND remoteKeyType = :remoteKeyType")
    suspend fun deleteByResourceId(resourceId: String, remoteKeyType: RemoteKeyType)

    @Query("DELETE FROM remote_keys WHERE remoteKeyType = :remoteKeyType AND resourceId IN (:resourceIds)")
    suspend fun deleteAllByResourceId(resourceIds: List<String>, remoteKeyType: RemoteKeyType)

    @Query("DELETE FROM remote_keys WHERE remoteKeyType = :remoteKeyType")
    suspend fun clearRemoteKeys(remoteKeyType: RemoteKeyType)

    @Query("DELETE FROM remote_keys")
    suspend fun clearAllRemoteKeys()
}