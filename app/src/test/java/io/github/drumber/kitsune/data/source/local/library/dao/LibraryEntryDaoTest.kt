package io.github.drumber.kitsune.data.source.local.library.dao

import androidx.room.Room
import io.github.drumber.kitsune.data.mapper.LibraryMapper.toLocalLibraryEntry
import io.github.drumber.kitsune.data.source.local.LocalDatabase
import io.github.drumber.kitsune.data.source.local.library.model.LocalLibraryEntry
import io.github.drumber.kitsune.data.source.local.library.model.LocalLibraryMedia.MediaType
import io.github.drumber.kitsune.data.source.local.library.model.LocalLibraryStatus
import io.github.drumber.kitsune.testutils.networkAnime
import io.github.drumber.kitsune.testutils.networkLibraryEntry
import io.github.drumber.kitsune.testutils.networkManga
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
class LibraryEntryDaoTest {

    private val faker = Faker()
    private lateinit var database: LocalDatabase
    private lateinit var dao: LibraryEntryDao

    @Before
    fun setup() {
        database = Room.inMemoryDatabaseBuilder(
            RuntimeEnvironment.getApplication(),
            LocalDatabase::class.java
        ).allowMainThreadQueries().build()
        dao = database.libraryEntryDao()
    }

    @After
    fun tearDown() {
        database.close()
    }

    private fun entry(
        id: String,
        status: LocalLibraryStatus = LocalLibraryStatus.Current,
        type: MediaType = MediaType.Anime,
        updatedAt: String = "2024-01-01T00:00:00.000Z"
    ): LocalLibraryEntry {
        val media = if (type == MediaType.Anime) networkAnime(faker) else networkManga(faker)
        return networkLibraryEntry(faker, media)
            .toLocalLibraryEntry()
            .copy(id = id, status = status, updatedAt = updatedAt)
    }

    @Test
    fun shouldInsertAndRetrieveSingleEntry() = runTest {
        // given
        val libraryEntry = entry(id = "1")

        // when
        dao.insertSingle(libraryEntry)

        // then
        assertThat(dao.getLibraryEntry("1")).isEqualTo(libraryEntry)
    }

    @Test
    fun shouldReturnNull_forUnknownId() = runTest {
        assertThat(dao.getLibraryEntry("missing")).isNull()
    }

    @Test
    fun shouldReplaceEntry_onConflictingId() = runTest {
        // given
        dao.insertSingle(entry(id = "1", status = LocalLibraryStatus.Current))

        // when the same id is inserted again
        val updated = entry(id = "1", status = LocalLibraryStatus.Completed)
        dao.insertSingle(updated)

        // then the entry is replaced, not duplicated
        assertThat(dao.getAllLibraryEntries()).hasSize(1)
        assertThat(dao.getLibraryEntry("1")?.status).isEqualTo(LocalLibraryStatus.Completed)
    }

    @Test
    fun shouldOrderEntries_byStatusThenUpdatedAtDesc() = runTest {
        // given entries across statuses and update times
        val planned = entry(id = "planned", status = LocalLibraryStatus.Planned)
        val currentOlder = entry(
            id = "current-old",
            status = LocalLibraryStatus.Current,
            updatedAt = "2024-01-01T00:00:00.000Z"
        )
        val currentNewer = entry(
            id = "current-new",
            status = LocalLibraryStatus.Current,
            updatedAt = "2024-06-01T00:00:00.000Z"
        )
        dao.insertAll(listOf(planned, currentOlder, currentNewer))

        // when
        val result = dao.getAllLibraryEntries()

        // then Current (orderId 0) before Planned (orderId 1); within Current, newer first
        assertThat(result.map { it.id })
            .containsExactly("current-new", "current-old", "planned")
    }

    @Test
    fun shouldFilterEntries_byType() = runTest {
        // given
        dao.insertAll(
            listOf(
                entry(id = "anime-1", type = MediaType.Anime),
                entry(id = "manga-1", type = MediaType.Manga)
            )
        )

        // when
        val animeEntries = dao.getAllLibraryEntriesByType(MediaType.Anime)

        // then
        assertThat(animeEntries.map { it.id }).containsExactly("anime-1")
    }

    @Test
    fun shouldFilterEntries_byStatus() = runTest {
        // given
        dao.insertAll(
            listOf(
                entry(id = "current", status = LocalLibraryStatus.Current),
                entry(id = "completed", status = LocalLibraryStatus.Completed)
            )
        )

        // when
        val result = dao.getAllLibraryEntriesByStatus(listOf(LocalLibraryStatus.Completed))

        // then
        assertThat(result.map { it.id }).containsExactly("completed")
    }

    @Test
    fun shouldFilterEntries_byTypeAndStatus() = runTest {
        // given
        dao.insertAll(
            listOf(
                entry(id = "anime-current", type = MediaType.Anime, status = LocalLibraryStatus.Current),
                entry(id = "anime-done", type = MediaType.Anime, status = LocalLibraryStatus.Completed),
                entry(id = "manga-current", type = MediaType.Manga, status = LocalLibraryStatus.Current)
            )
        )

        // when
        val result = dao.getAllLibraryEntriesByTypeAndStatus(
            MediaType.Anime,
            listOf(LocalLibraryStatus.Current)
        )

        // then
        assertThat(result.map { it.id }).containsExactly("anime-current")
    }

    @Test
    fun shouldRetrieveEntry_byMediaId() = runTest {
        // given
        val libraryEntry = entry(id = "1")
        val mediaId = requireNotNull(libraryEntry.media?.id)
        dao.insertSingle(libraryEntry)

        // when
        val result = dao.getLibraryEntryFromMedia(mediaId)

        // then
        assertThat(result?.id).isEqualTo("1")
    }

    @Test
    fun shouldReportWhetherEntryIsNewer_thanGivenTimestamp() = runTest {
        // given
        dao.insertSingle(entry(id = "1", updatedAt = "2024-06-01T00:00:00.000Z"))

        // then
        assertThat(dao.hasLibraryEntryWhereUpdatedAtIsAfter("1", "2024-01-01T00:00:00.000Z")).isTrue()
        assertThat(dao.hasLibraryEntryWhereUpdatedAtIsAfter("1", "2024-12-01T00:00:00.000Z")).isFalse()
    }

    @Test
    fun shouldUpdateExistingEntry() = runTest {
        // given
        dao.insertSingle(entry(id = "1", status = LocalLibraryStatus.Current))

        // when
        dao.updateSingle(entry(id = "1", status = LocalLibraryStatus.Dropped))

        // then
        assertThat(dao.getLibraryEntry("1")?.status).isEqualTo(LocalLibraryStatus.Dropped)
    }

    @Test
    fun shouldDeleteEntries() = runTest {
        // given
        val first = entry(id = "1")
        val second = entry(id = "2")
        val third = entry(id = "3")
        dao.insertAll(listOf(first, second, third))

        // when
        dao.deleteSingle(first)
        dao.deleteSingleById("2")

        // then
        assertThat(dao.getAllLibraryEntries().map { it.id }).containsExactly("3")

        // and clearing removes the rest
        dao.deleteAll(listOf(third))
        assertThat(dao.getAllLibraryEntries()).isEmpty()
    }

    @Test
    fun shouldClearAllEntries() = runTest {
        // given
        dao.insertAll(listOf(entry(id = "1"), entry(id = "2")))

        // when
        dao.clearLibraryEntries()

        // then
        assertThat(dao.getAllLibraryEntries()).isEmpty()
    }
}
