package io.github.drumber.kitsune.data.room

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import io.github.drumber.kitsune.data.model.RemoteKey
import io.github.drumber.kitsune.data.model.library.LibraryEntry
import io.github.drumber.kitsune.data.model.library.OfflineLibraryUpdate

@Database(
    entities = [LibraryEntry::class, RemoteKey::class, OfflineLibraryUpdate::class],
    version = 23,
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class ResourceDatabase : RoomDatabase() {

    abstract fun libraryEntryDao(): LibraryEntryDao
    abstract fun remoteKeys(): RemoteKeyDao
    abstract fun offlineLibraryEntryDao(): OfflineLibraryUpdateDao

}