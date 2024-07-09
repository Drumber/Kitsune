package io.github.drumber.kitsune.data.source.local.library.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import io.github.drumber.kitsune.data.source.local.library.dao.LibraryEntryDao.Companion.ORDER_BY_STATUS
import io.github.drumber.kitsune.data.source.local.library.model.LocalLibraryEntryWithModification
import io.github.drumber.kitsune.data.source.local.library.model.LocalLibraryStatus

@Dao
interface LibraryEntryWithModificationDao {

    @Transaction
    @Query("SELECT * FROM library_entries WHERE media_id = :mediaId")
    suspend fun getLibraryEntryWithModificationFromMedia(mediaId: String): LocalLibraryEntryWithModification?

    @Transaction
    @Query("SELECT * FROM library_entries WHERE media_id = :mediaId")
    fun getLibraryEntryWithModificationFromMediaAsLiveData(mediaId: String): LiveData<LocalLibraryEntryWithModification?>

    @Transaction
    @Query("SELECT * FROM library_entries WHERE status IN (:status) $ORDER_BY_STATUS")
    suspend fun getLibraryEntriesWithModificationByStatus(status: List<LocalLibraryStatus>): List<LocalLibraryEntryWithModification>
}