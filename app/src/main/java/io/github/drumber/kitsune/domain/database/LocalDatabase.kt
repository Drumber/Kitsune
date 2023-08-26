package io.github.drumber.kitsune.domain.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import io.github.drumber.kitsune.domain.model.database.LocalAnime
import io.github.drumber.kitsune.domain.model.database.LocalLibraryEntry
import io.github.drumber.kitsune.domain.model.database.LocalLibraryEntryModification
import io.github.drumber.kitsune.domain.model.database.LocalManga

@Database(
    entities = [
        LocalLibraryEntry::class, LocalLibraryEntryModification::class,
        LocalAnime::class, LocalManga::class
    ],
    version = 1,
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class LocalDatabase : RoomDatabase() {

    abstract fun libraryEntryDao(): LibraryEntryDao
    abstract fun libraryEntryModificationDao(): LibraryEntryModificationDao

}