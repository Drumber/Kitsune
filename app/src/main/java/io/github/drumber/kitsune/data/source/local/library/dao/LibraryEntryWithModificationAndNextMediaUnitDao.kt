package io.github.drumber.kitsune.data.source.local.library.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import io.github.drumber.kitsune.data.source.local.library.dao.LibraryEntryDao.Companion.ORDER_BY_STATUS
import io.github.drumber.kitsune.data.source.local.library.model.LocalLibraryEntryWithModificationAndNextMediaUnit
import io.github.drumber.kitsune.data.source.local.library.model.LocalLibraryStatus
import kotlinx.coroutines.flow.Flow

@Dao
interface LibraryEntryWithModificationAndNextMediaUnitDao {

    @Transaction
    @Query("SELECT * FROM library_entries WHERE status IN (:status) $ORDER_BY_STATUS")
    suspend fun getByStatus(status: List<LocalLibraryStatus>): List<LocalLibraryEntryWithModificationAndNextMediaUnit>

    @Transaction
    @Query("SELECT * FROM library_entries WHERE status IN (:status) $ORDER_BY_STATUS")
    fun getByStatusAsFlow(status: List<LocalLibraryStatus>): Flow<List<LocalLibraryEntryWithModificationAndNextMediaUnit>>
}