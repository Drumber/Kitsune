package io.github.drumber.kitsune.database

import android.content.Context
import android.database.sqlite.SQLiteConstraintException
import androidx.room.Room
import androidx.room.withTransaction
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.matcher.ViewMatchers.assertThat
import androidx.test.ext.junit.runners.AndroidJUnit4
import io.github.drumber.kitsune.data.source.local.LocalDatabase
import io.github.drumber.kitsune.data.source.local.library.dao.LibraryEntryDao
import io.github.drumber.kitsune.data.source.local.library.dao.LibraryEntryWithModificationAndNextMediaUnitDao
import io.github.drumber.kitsune.data.source.local.library.dao.NextMediaUnitDao
import io.github.drumber.kitsune.data.source.local.library.model.LocalLibraryEntry
import io.github.drumber.kitsune.data.source.local.library.model.LocalLibraryStatus
import io.github.drumber.kitsune.data.source.local.library.model.LocalNextMediaUnit
import kotlinx.coroutines.test.runTest
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.notNullValue
import org.hamcrest.CoreMatchers.nullValue
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException

@RunWith(AndroidJUnit4::class)
class LocalNextMediaUnitTest {

    private lateinit var db: LocalDatabase
    private lateinit var nextMediaUnitDao: NextMediaUnitDao
    private lateinit var libraryEntryDao: LibraryEntryDao
    private lateinit var libraryEntryWithModificationAndNextMediaUnitDao: LibraryEntryWithModificationAndNextMediaUnitDao

    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, LocalDatabase::class.java).build()
        nextMediaUnitDao = db.nextMediaUnitDao()
        libraryEntryDao = db.libraryEntryDao()
        libraryEntryWithModificationAndNextMediaUnitDao =
            db.libraryEntryWithModificationAndNextMediaUnitDao()
    }

    @After
    @Throws(IOException::class)
    fun closeDb() {
        db.close()
    }

    @Test
    fun shouldInsertWhenLibraryEntryIsPresent() = runTest {
        // given
        val localLibraryEntry = createLocalLibraryEntry()
        val localNextMediaUnit = createLocalNextMediaUnit(libraryEntryId = localLibraryEntry.id)

        libraryEntryDao.insertSingle(localLibraryEntry)

        // when
        nextMediaUnitDao.insertSingle(localNextMediaUnit)

        // then
        val getNextMediaUnit = nextMediaUnitDao.getSingle(localNextMediaUnit.id)
        assertThat(getNextMediaUnit, `is`(equalTo(localNextMediaUnit)))
    }

    @Test(expected = SQLiteConstraintException::class)
    fun shouldFailToInsertWhenLibraryEntryDoesNotExist() = runTest {
        // given
        val localNextMediaUnit = createLocalNextMediaUnit(libraryEntryId = "non_existent")

        // when & then
        nextMediaUnitDao.insertSingle(localNextMediaUnit)
    }

    @Test
    fun shouldCascadeDelete() = runTest {
        // given
        val localLibraryEntry = createLocalLibraryEntry()
        val localNextMediaUnit = createLocalNextMediaUnit(libraryEntryId = localLibraryEntry.id)

        db.withTransaction {
            libraryEntryDao.insertSingle(localLibraryEntry)
            nextMediaUnitDao.insertSingle(localNextMediaUnit)
        }

        // when
        libraryEntryDao.deleteSingle(localLibraryEntry)

        // then
        val getNextMediaUnit = nextMediaUnitDao.getSingle(localNextMediaUnit.id)
        assertThat(getNextMediaUnit, `is`(nullValue()))
    }

    @Test
    fun shouldGetLibraryEntryWithNextMediaUnit() = runTest {
        // given
        val localLibraryEntry = createLocalLibraryEntry()
        val localNextMediaUnit = createLocalNextMediaUnit(libraryEntryId = localLibraryEntry.id)

        db.withTransaction {
            libraryEntryDao.insertSingle(localLibraryEntry)
            nextMediaUnitDao.insertSingle(localNextMediaUnit)
        }

        // when
        val libraryEntryWithNextMediaUnit = libraryEntryWithModificationAndNextMediaUnitDao
            .getByStatus(LocalLibraryStatus.entries)
            .firstOrNull()

        // then
        assertThat(libraryEntryWithNextMediaUnit, `is`(notNullValue()))
        assertThat(libraryEntryWithNextMediaUnit?.libraryEntry, `is`(equalTo(localLibraryEntry)))
        assertThat(libraryEntryWithNextMediaUnit?.nextMediaUnit, `is`(equalTo(localNextMediaUnit)))
    }

    private fun createLocalLibraryEntry(id: String = "le1") = LocalLibraryEntry(
        id = id,
        updatedAt = null,
        startedAt = null,
        finishedAt = null,
        progressedAt = null,
        status = LocalLibraryStatus.Current,
        progress = null,
        reconsuming = null,
        reconsumeCount = null,
        volumesOwned = null,
        ratingTwenty = null,
        notes = null,
        privateEntry = null,
        reactionSkipped = null,
        media = null
    )

    private fun createLocalNextMediaUnit(id: String = "nmu1", libraryEntryId: String) =
        LocalNextMediaUnit(
            id = id,
            libraryEntryId = libraryEntryId,
            titles = null,
            canonicalTitle = null,
            number = null,
            thumbnail = null
        )
}