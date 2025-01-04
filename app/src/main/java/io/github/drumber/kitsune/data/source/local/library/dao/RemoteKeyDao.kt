package io.github.drumber.kitsune.data.source.local.library.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Query
import androidx.room.Upsert
import io.github.drumber.kitsune.data.source.local.library.model.RemoteKeyEntity

@Dao
interface RemoteKeyDao {

    @Query("SELECT * FROM remote_keys WHERE resourceId = :resourceId AND requestType = :requestType")
    suspend fun getRemoteKeyByResourceId(resourceId: String, requestType: String): RemoteKeyEntity?

    @Upsert
    suspend fun insertALl(key: List<RemoteKeyEntity>)

    @Query("DELETE FROM remote_keys WHERE resourceId = :resourceId AND requestType = :requestType")
    suspend fun deleteByResourceId(resourceId: String, requestType: String)

    @Query("DELETE FROM remote_keys WHERE requestType = :requestType AND resourceId IN (:resourceIds)")
    suspend fun deleteAllByResourceId(resourceIds: List<String>, requestType: String)

    @Delete
    suspend fun deleteSingle(remoteKey: RemoteKeyEntity)

    @Query("DELETE FROM remote_keys WHERE requestType = :requestType")
    suspend fun clearRemoteKeys(requestType: String)

    @Query("DELETE FROM remote_keys")
    suspend fun clearAllRemoteKeys()
}