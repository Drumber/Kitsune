package io.github.drumber.kitsune.data.source.local.library.dao

import androidx.room.Room
import app.cash.turbine.test
import io.github.drumber.kitsune.data.source.local.LocalDatabase
import io.github.drumber.kitsune.data.source.local.library.model.LocalLibraryEntryModification
import io.github.drumber.kitsune.data.source.local.library.model.LocalLibraryModificationState
import io.github.drumber.kitsune.testutils.localLibraryEntryModification
import kotlinx.coroutines.test.runTest
import net.datafaker.Faker
import org.assertj.core.api.Assertions.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment

@RunWith(RobolectricTestRunner::class)
class LibraryEntryModificationDaoTest {

    private val faker = Faker()
    private lateinit var database: LocalDatabase
    private lateinit var dao: LibraryEntryModificationDao

    @Before
    fun setup() {
        database = Room.inMemoryDatabaseBuilder(
            RuntimeEnvironment.getApplication(),
            LocalDatabase::class.java
        ).allowMainThreadQueries().build()
        dao = database.libraryEntryModificationDao()
    }

    @After
    fun tearDown() {
        database.close()
    }

    private fun modification(
        id: String,
        state: LocalLibraryModificationState = LocalLibraryModificationState.NOT_SYNCHRONIZED,
        createTime: Long = 100L
    ): LocalLibraryEntryModification =
        localLibraryEntryModification(faker).copy(id = id, state = state, createTime = createTime)

    @Test
    fun shouldInsertAndRetrieveSingleModification() = runTest {
        // given
        val mod = modification(id = "1")

        // when
        dao.insertSingle(mod)

        // then
        assertThat(dao.getLibraryEntryModification("1")).isEqualTo(mod)
    }

    @Test
    fun shouldRetrieveAllModifications() = runTest {
        // given
        dao.insertSingle(modification(id = "1"))
        dao.insertSingle(modification(id = "2"))

        // when
        val result = dao.getAllLibraryEntryModifications()

        // then
        assertThat(result.map { it.id }).containsExactlyInAnyOrder("1", "2")
    }

    @Test
    fun shouldReplaceModification_onConflictingId() = runTest {
        // given
        dao.insertSingle(modification(id = "1", state = LocalLibraryModificationState.NOT_SYNCHRONIZED))

        // when
        dao.insertSingle(modification(id = "1", state = LocalLibraryModificationState.SYNCHRONIZING))

        // then
        assertThat(dao.getAllLibraryEntryModifications()).hasSize(1)
        assertThat(dao.getLibraryEntryModification("1")?.state)
            .isEqualTo(LocalLibraryModificationState.SYNCHRONIZING)
    }

    @Test
    fun shouldUpdateModification() = runTest {
        // given
        dao.insertSingle(modification(id = "1", state = LocalLibraryModificationState.NOT_SYNCHRONIZED))

        // when
        dao.updateSingle(modification(id = "1", state = LocalLibraryModificationState.SYNCHRONIZING))

        // then
        assertThat(dao.getLibraryEntryModification("1")?.state)
            .isEqualTo(LocalLibraryModificationState.SYNCHRONIZING)
    }

    @Test
    fun shouldDeleteModification_byInstanceAndById() = runTest {
        // given
        val first = modification(id = "1")
        dao.insertSingle(first)
        dao.insertSingle(modification(id = "2"))

        // when
        dao.deleteSingle(first)
        dao.deleteSingleById("2")

        // then
        assertThat(dao.getAllLibraryEntryModifications()).isEmpty()
    }

    @Test
    fun shouldDeleteOnlyWhenCreateTimeMatches() = runTest {
        // given
        dao.insertSingle(modification(id = "1", createTime = 100L))

        // when a stale create time is used
        dao.deleteSingleMatchingCreateTime("1", createTime = 999L)

        // then nothing is deleted
        assertThat(dao.getLibraryEntryModification("1")).isNotNull

        // when the matching create time is used
        dao.deleteSingleMatchingCreateTime("1", createTime = 100L)

        // then the modification is deleted
        assertThat(dao.getLibraryEntryModification("1")).isNull()
    }

    @Test
    fun shouldClearAllModifications() = runTest {
        // given
        dao.insertSingle(modification(id = "1"))
        dao.insertSingle(modification(id = "2"))

        // when
        dao.clearAll()

        // then
        assertThat(dao.getAllLibraryEntryModifications()).isEmpty()
    }

    @Test
    fun shouldEmitModifications_throughFlow() = runTest {
        // given an empty table
        dao.getAllLibraryEntryModificationsAsFlow().test {
            assertThat(awaitItem()).isEmpty()

            // when a modification is inserted
            dao.insertSingle(modification(id = "1"))

            // then the flow re-emits with the new state
            assertThat(awaitItem().map { it.id }).containsExactly("1")

            cancelAndIgnoreRemainingEvents()
        }
    }
}
