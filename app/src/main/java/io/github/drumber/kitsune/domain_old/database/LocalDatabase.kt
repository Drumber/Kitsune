package io.github.drumber.kitsune.domain_old.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import io.github.drumber.kitsune.domain_old.model.database.LocalLibraryEntry
import io.github.drumber.kitsune.domain_old.model.database.LocalLibraryEntryModification
import io.github.drumber.kitsune.domain_old.model.database.RemoteKeyEntity

@Database(
    entities = [
        LocalLibraryEntry::class, LocalLibraryEntryModification::class, RemoteKeyEntity::class
    ],
    version = 2,
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class LocalDatabase : RoomDatabase() {

    abstract fun libraryEntryDao(): LibraryEntryDao
    abstract fun libraryEntryModificationDao(): LibraryEntryModificationDao
    abstract fun libraryEntryWithModification(): LibraryEntryWithModificationDao
    abstract fun remoteKeyDao(): RemoteKeyDao

}