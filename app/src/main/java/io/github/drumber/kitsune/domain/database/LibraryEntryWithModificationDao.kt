package io.github.drumber.kitsune.domain.database

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import io.github.drumber.kitsune.domain.model.database.LibraryEntryWithModification

@Dao
interface LibraryEntryWithModificationDao {

    @Transaction
    @Query("SELECT * FROM library_entries WHERE anime_id = :mediaId OR manga_id = :mediaId")
    fun getLibraryEntryWithModificationFromMediaLiveData(mediaId: String): LiveData<LibraryEntryWithModification?>

}