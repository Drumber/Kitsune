package io.github.drumber.kitsune.data.room

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import io.github.drumber.kitsune.data.model.RemoteKey
import io.github.drumber.kitsune.data.model.library.LibraryEntry

@Database(
    entities = [LibraryEntry::class, RemoteKey::class],
    version = 8,
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class ResourceDatabase : RoomDatabase() {

    abstract fun libraryEntryDao(): LibraryEntryDao
    abstract fun remoteKeys(): RemoteKeyDao

}