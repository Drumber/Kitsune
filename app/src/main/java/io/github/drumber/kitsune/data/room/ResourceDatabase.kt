package io.github.drumber.kitsune.data.room

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import io.github.drumber.kitsune.data.model.RemoteKey
import io.github.drumber.kitsune.data.model.resource.Anime
import io.github.drumber.kitsune.data.model.resource.Manga

@Database(
    entities = [Anime::class, Manga::class, RemoteKey::class],
    version = 2,
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class ResourceDatabase : RoomDatabase() {

    abstract fun animeDao(): AnimeDao
    abstract fun mangaDao(): MangaDao
    abstract fun remoteKeys(): RemoteKeyDao

}