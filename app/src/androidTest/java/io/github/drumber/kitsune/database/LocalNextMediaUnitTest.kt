package io.github.drumber.kitsune.database

import android.content.Context
import android.database.sqlite.SQLiteConstraintException
import androidx.paging.PagingSource
import androidx.room.Room
import androidx.room.withTransaction
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.matcher.ViewMatchers.assertThat
import androidx.test.ext.junit.runners.AndroidJUnit4
import io.github.drumber.kitsune.data.common.library.LibraryFilterOptions.SortBy
import io.github.drumber.kitsune.data.common.library.LibraryFilterOptions.SortDirection
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

    @Test
    fun shouldSortLibraryEntries() = runTest {
        // given
        val samples = listOf(
            createLocalLibraryEntry(
                "le0",
                updatedAt = "2024-01-01",
                startedAt = "2024-12-01",
                progress = 3,
                ratingTwenty = 1
            ),
            createLocalLibraryEntry(
                "le1",
                updatedAt = "2024-01-02",
                startedAt = "2024-11-01",
                progress = 1,
                ratingTwenty = 0
            ),
            createLocalLibraryEntry(
                "le2",
                updatedAt = "2024-01-03",
                startedAt = "2024-10-01",
                progress = 0,
                ratingTwenty = 3
            ),
            createLocalLibraryEntry(
                "le3",
                updatedAt = "2024-01-04",
                startedAt = "2024-09-01",
                progress = 2,
                ratingTwenty = 2
            )
        )

        db.withTransaction {
            samples.forEach { entry ->
                libraryEntryDao.insertSingle(entry)
                nextMediaUnitDao.insertSingle(
                    createLocalNextMediaUnit(
                        id = "nmu_${entry.id}",
                        libraryEntryId = entry.id
                    )
                )
            }
        }

        val getIds: (suspend (SortBy, direction: SortDirection) -> List<String>) =
            { sortBy, direction ->
                val libraryEntriesWithNextMediaUnit =
                    libraryEntryWithModificationAndNextMediaUnitDao.getByFilterAsPagingSource(
                        status = null,
                        mediaType = null,
                        sortBy = sortBy,
                        sortDirection = direction
                    ).load(
                        PagingSource.LoadParams.Refresh(
                            null,
                            samples.size,
                            false
                        )
                    ) as PagingSource.LoadResult.Page

                libraryEntriesWithNextMediaUnit.data.map { it.libraryEntry.id }
            }

        val getTestInstance: (suspend (SortBy, List<String>) -> Triple<List<String>, List<String>, List<String>>) =
            { sortBy, expectedAsc ->
                Triple(
                    getIds(sortBy, SortDirection.ASC),
                    getIds(sortBy, SortDirection.DESC),
                    expectedAsc
                )
            }

        // when
        val byStatus = getTestInstance(SortBy.STATUS, listOf("le0", "le1", "le2", "le3"))
        val byStartedAt = getTestInstance(SortBy.STARTED_AT, listOf("le3", "le2", "le1", "le0"))
        val byUpdatedAt = getTestInstance(SortBy.UPDATED_AT, listOf("le0", "le1", "le2", "le3"))
        val byProgress = getTestInstance(SortBy.PROGRESS, listOf("le2", "le1", "le3", "le0"))
        val byRating = getTestInstance(SortBy.RATING, listOf("le1", "le0", "le3", "le2"))

        // then
        assertThat(byStatus.first, equalTo(byStatus.third))
        assertThat(byStatus.second, equalTo(byStatus.third.reversed()))
        assertThat(byStartedAt.first, equalTo(byStartedAt.third))
        assertThat(byStartedAt.second, equalTo(byStartedAt.third.reversed()))
        assertThat(byUpdatedAt.first, equalTo(byUpdatedAt.third))
        assertThat(byUpdatedAt.second, equalTo(byUpdatedAt.third.reversed()))
        assertThat(byProgress.first, equalTo(byProgress.third))
        assertThat(byProgress.second, equalTo(byProgress.third.reversed()))
        assertThat(byRating.first, equalTo(byRating.third))
        assertThat(byRating.second, equalTo(byRating.third.reversed()))
    }

    private fun createLocalLibraryEntry(
        id: String = "le1",
        updatedAt: String? = null,
        startedAt: String? = null,
        progress: Int? = null,
        ratingTwenty: Int? = null
    ) = LocalLibraryEntry(
        id = id,
        updatedAt = updatedAt,
        startedAt = startedAt,
        finishedAt = null,
        progressedAt = null,
        status = LocalLibraryStatus.Current,
        progress = progress,
        reconsuming = null,
        reconsumeCount = null,
        volumesOwned = null,
        ratingTwenty = ratingTwenty,
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