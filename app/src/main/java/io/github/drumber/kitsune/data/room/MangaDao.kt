package io.github.drumber.kitsune.data.room

import androidx.paging.PagingSource
import androidx.room.*
import io.github.drumber.kitsune.data.model.resource.Manga

@Dao
interface MangaDao {

    @Query("SELECT * FROM manga_table")
    fun getManga(): PagingSource<Int, Manga>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(manga: List<Manga>)

    @Delete
    suspend fun delete(manga: Manga)

    @Query("DELETE FROM manga_table")
    suspend fun clearAllManga()

}