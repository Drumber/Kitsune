package io.github.drumber.kitsune.domain.database

import androidx.room.Database
import androidx.room.TypeConverters
import io.github.drumber.kitsune.domain.model.database.LocalLibraryEntry
import io.github.drumber.kitsune.domain.model.database.LocalLibraryEntryModification

@Database(
    entities = [LocalLibraryEntry::class, LocalLibraryEntryModification::class],
    version = 1,
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class LocalDatabase {

    abstract fun libraryEntryDao(): LibraryEntryDao
    abstract fun libraryEntryModificationDao(): LibraryEntryModificationDao

}