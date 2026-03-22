package io.github.drumber.kitsune.data.source.local

import android.app.Application
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import io.github.drumber.kitsune.data.source.local.library.LocalLibraryConverters
import io.github.drumber.kitsune.data.source.local.library.dao.LibraryEntryDao
import io.github.drumber.kitsune.data.source.local.library.dao.LibraryEntryModificationDao
import io.github.drumber.kitsune.data.source.local.library.dao.LibraryEntryWithModificationAndNextMediaUnitDao
import io.github.drumber.kitsune.data.source.local.library.dao.LibraryEntryWithModificationDao
import io.github.drumber.kitsune.data.source.local.library.dao.NextMediaUnitDao
import io.github.drumber.kitsune.data.source.local.library.dao.RemoteKeyDao
import io.github.drumber.kitsune.data.source.local.library.model.LocalLibraryEntry
import io.github.drumber.kitsune.data.source.local.library.model.LocalLibraryEntryModification
import io.github.drumber.kitsune.data.source.local.library.model.LocalNextMediaUnit
import io.github.drumber.kitsune.data.source.local.library.model.RemoteKeyEntity

@Database(
    entities = [
        LocalLibraryEntry::class,
        LocalLibraryEntryModification::class,
        LocalNextMediaUnit::class,
        RemoteKeyEntity::class
    ],
    version = 4,
    exportSchema = true
)
@TypeConverters(LocalLibraryConverters::class)
abstract class LocalDatabase : RoomDatabase() {

    abstract fun libraryEntryDao(): LibraryEntryDao
    abstract fun libraryEntryModificationDao(): LibraryEntryModificationDao
    abstract fun libraryEntryWithModificationDao(): LibraryEntryWithModificationDao
    abstract fun libraryEntryWithModificationAndNextMediaUnitDao(): LibraryEntryWithModificationAndNextMediaUnitDao
    abstract fun nextMediaUnitDao(): NextMediaUnitDao
    abstract fun remoteKeyDao(): RemoteKeyDao

    companion object {
        fun createLocalDatabase(application: Application): LocalDatabase {
            return Room.databaseBuilder(application, LocalDatabase::class.java, "kitsune.db")
                .fallbackToDestructiveMigration()
                .build()
        }
    }
}