package io.github.drumber.kitsune.domain.room

import androidx.room.*
import androidx.room.migration.AutoMigrationSpec
import io.github.drumber.kitsune.domain.database.Converters
import io.github.drumber.kitsune.domain.model.RemoteKey
import io.github.drumber.kitsune.domain.model.library.LibraryEntry
import io.github.drumber.kitsune.domain.model.library.LibraryModification

//@Database(
//    entities = [LibraryEntry::class, RemoteKey::class, LibraryModification::class],
//    version = 26,
//    exportSchema = true,
//    autoMigrations = [
//        AutoMigration(from = 24, to = 25, spec = ResourceDatabase.MigrationSpec_24_25::class),
//        AutoMigration(from = 25, to = 26, spec = ResourceDatabase.MigrationSpec_25_26::class)
//    ]
//)
@TypeConverters(Converters::class)
abstract class ResourceDatabase : RoomDatabase() {

    abstract fun libraryEntryDao(): LibraryEntryDao
    abstract fun remoteKeys(): RemoteKeyDao
    abstract fun offlineLibraryModificationDao(): OfflineLibraryModificationDao

    @DeleteTable(tableName = "offline_library_update")
    class MigrationSpec_24_25 : AutoMigrationSpec

    @RenameColumn(
        tableName = "library_table",
        fromColumnName = "isPrivate",
        toColumnName = "privateEntry"
    )
    class MigrationSpec_25_26 : AutoMigrationSpec

}