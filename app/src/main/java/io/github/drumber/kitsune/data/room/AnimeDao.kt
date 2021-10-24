package io.github.drumber.kitsune.data.room

import androidx.paging.PagingSource
import androidx.room.*
import io.github.drumber.kitsune.data.model.resource.Anime

@Dao
interface AnimeDao {

    @Query("SELECT * FROM anime_table")
    fun getAnime(): PagingSource<Int, Anime>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(anime: List<Anime>)

    @Delete
    suspend fun delete(anime: Anime)

    @Query("DELETE FROM anime_table")
    suspend fun clearAllAnime()

}