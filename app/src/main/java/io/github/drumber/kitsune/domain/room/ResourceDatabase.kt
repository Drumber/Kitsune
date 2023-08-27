package io.github.drumber.kitsune.domain.room

import androidx.room.DeleteTable
import androidx.room.RenameColumn
import androidx.room.RoomDatabase
import androidx.room.migration.AutoMigrationSpec

//@Database(
//    entities = [LibraryEntry::class, RemoteKey::class, LibraryModification::class],
//    version = 26,
//    exportSchema = true,
//    autoMigrations = [
//        AutoMigration(from = 24, to = 25, spec = ResourceDatabase.MigrationSpec_24_25::class),
//        AutoMigration(from = 25, to = 26, spec = ResourceDatabase.MigrationSpec_25_26::class)
//    ]
//)
@Deprecated("ResourceDatabase is no longer used.", replaceWith = ReplaceWith("LocalDatabase"))
abstract class ResourceDatabase : RoomDatabase() {

    @DeleteTable(tableName = "offline_library_update")
    class MigrationSpec_24_25 : AutoMigrationSpec

    @RenameColumn(
        tableName = "library_table",
        fromColumnName = "isPrivate",
        toColumnName = "privateEntry"
    )
    class MigrationSpec_25_26 : AutoMigrationSpec

}