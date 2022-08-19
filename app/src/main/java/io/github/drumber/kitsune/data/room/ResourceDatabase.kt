package io.github.drumber.kitsune.data.room

import androidx.room.*
import androidx.room.migration.AutoMigrationSpec
import io.github.drumber.kitsune.data.model.RemoteKey
import io.github.drumber.kitsune.data.model.library.LibraryEntry
import io.github.drumber.kitsune.data.model.library.LibraryModification

@Database(
    entities = [LibraryEntry::class, RemoteKey::class, LibraryModification::class],
    version = 25,
    exportSchema = true,
    autoMigrations = [
        AutoMigration (from = 24, to = 25, spec = ResourceDatabase.MigrationSpec_24_25::class)
    ]
)
@TypeConverters(Converters::class)
abstract class ResourceDatabase : RoomDatabase() {

    abstract fun libraryEntryDao(): LibraryEntryDao
    abstract fun remoteKeys(): RemoteKeyDao
    abstract fun offlineLibraryModificationDao(): OfflineLibraryModificationDao

    @DeleteTable(tableName = "offline_library_update")
    class MigrationSpec_24_25 : AutoMigrationSpec

}