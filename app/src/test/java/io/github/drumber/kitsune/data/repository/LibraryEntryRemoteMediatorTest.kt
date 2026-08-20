package io.github.drumber.kitsune.data.repository

import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.PagingConfig
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import androidx.room.Room
import io.github.drumber.kitsune.config.Kitsu
import io.github.drumber.kitsune.data.common.library.LibraryEntryKind
import io.github.drumber.kitsune.data.presentation.model.library.LibraryEntryFilter
import io.github.drumber.kitsune.data.source.local.LocalDatabase
import io.github.drumber.kitsune.data.source.local.library.LibraryLocalDataSource
import io.github.drumber.kitsune.data.source.local.library.model.LocalLibraryEntry
import io.github.drumber.kitsune.data.source.local.library.model.RemoteKeyType
import io.github.drumber.kitsune.data.source.network.PageData
import io.github.drumber.kitsune.data.source.network.library.LibraryNetworkDataSource
import io.github.drumber.kitsune.data.source.network.library.model.NetworkLibraryEntry
import io.github.drumber.kitsune.testutils.networkAnime
import io.github.drumber.kitsune.testutils.networkLibraryEntry
import io.github.drumber.kitsune.testutils.onSuspend
import kotlinx.coroutines.test.runTest
import net.datafaker.Faker
import org.assertj.core.api.Assertions.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.doThrow
import org.mockito.kotlin.mock
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment

/**
 * Integration tests for [LibraryEntryRemoteMediator] using a real in-memory Room database and a
 * mocked network data source. Exercises the REFRESH/APPEND/PREPEND load types and the
 * end-of-pagination signalling.
 */
@OptIn(ExperimentalPagingApi::class)
@RunWith(RobolectricTestRunner::class)
class LibraryEntryRemoteMediatorTest {

    private val faker = Faker()
    private lateinit var database: LocalDatabase
    private lateinit var localDataSource: LibraryLocalDataSource

    private val filter = LibraryEntryFilter(LibraryEntryKind.All, emptyList())

    @Before
    fun setup() {
        database = Room.inMemoryDatabaseBuilder(
            RuntimeEnvironment.getApplication(),
            LocalDatabase::class.java
        ).allowMainThreadQueries().build()
        localDataSource = LibraryLocalDataSource(database)
    }

    @After
    fun tearDown() {
        database.close()
    }

    private fun networkEntry(id: String): NetworkLibraryEntry =
        networkLibraryEntry(faker, networkAnime(faker)).copy(id = id)

    private fun pageData(
        data: List<NetworkLibraryEntry>?,
        prev: Int? = null,
        next: Int? = null
    ) = PageData(data = data, first = null, last = null, prev = prev, next = next)

    private fun emptyState() = PagingState<Int, LocalLibraryEntry>(
        pages = emptyList(),
        anchorPosition = null,
        config = PagingConfig(pageSize = Kitsu.DEFAULT_PAGE_SIZE_LIBRARY),
        leadingPlaceholderCount = 0
    )

    private fun mediator(networkDataSource: LibraryNetworkDataSource) =
        LibraryEntryRemoteMediator(filter, networkDataSource, localDataSource)

    @Test
    fun shouldReturnEndOfPagination_onPrepend() = runTest {
        // given
        val networkDataSource = mock<LibraryNetworkDataSource>()

        // when
        val result = mediator(networkDataSource).load(LoadType.PREPEND, emptyState())

        // then
        assertThat(result).isInstanceOf(RemoteMediator.MediatorResult.Success::class.java)
        assertThat((result as RemoteMediator.MediatorResult.Success).endOfPaginationReached).isTrue()
    }

    @Test
    fun shouldPersistEntriesAndRemoteKeys_onRefresh_andSignalEndWhenNoNextPage() = runTest {
        // given
        val networkDataSource = mock<LibraryNetworkDataSource> {
            onSuspend { getAllLibraryEntries(any()) } doReturn pageData(
                listOf(networkEntry("1"), networkEntry("2")),
                next = null
            )
        }

        // when
        val result = mediator(networkDataSource).load(LoadType.REFRESH, emptyState())

        // then
        assertThat(result).isInstanceOf(RemoteMediator.MediatorResult.Success::class.java)
        assertThat((result as RemoteMediator.MediatorResult.Success).endOfPaginationReached).isTrue()
        // entries are written to the database
        assertThat(localDataSource.getLibraryEntry("1")).isNotNull
        assertThat(localDataSource.getLibraryEntry("2")).isNotNull
        // remote keys are written for each entry
        assertThat(localDataSource.getRemoteKeyByResourceId("1", RemoteKeyType.LibraryEntry)).isNotNull
        assertThat(localDataSource.getRemoteKeyByResourceId("2", RemoteKeyType.LibraryEntry)).isNotNull
    }

    @Test
    fun shouldNotSignalEndOfPagination_whenNextPageExists() = runTest {
        // given
        val networkDataSource = mock<LibraryNetworkDataSource> {
            onSuspend { getAllLibraryEntries(any()) } doReturn pageData(
                listOf(networkEntry("1")),
                next = 1
            )
        }

        // when
        val result = mediator(networkDataSource).load(LoadType.REFRESH, emptyState())

        // then
        assertThat((result as RemoteMediator.MediatorResult.Success).endOfPaginationReached).isFalse()
    }

    @Test
    fun shouldReturnError_whenNetworkThrows() = runTest {
        // given
        val exception = RuntimeException("boom")
        val networkDataSource = mock<LibraryNetworkDataSource> {
            onSuspend { getAllLibraryEntries(any()) } doThrow exception
        }

        // when
        val result = mediator(networkDataSource).load(LoadType.REFRESH, emptyState())

        // then
        assertThat(result).isInstanceOf(RemoteMediator.MediatorResult.Error::class.java)
        assertThat((result as RemoteMediator.MediatorResult.Error).throwable).isEqualTo(exception)
    }

    @Test
    fun shouldReturnError_whenDataIsNull() = runTest {
        // given
        val networkDataSource = mock<LibraryNetworkDataSource> {
            onSuspend { getAllLibraryEntries(any()) } doReturn pageData(data = null)
        }

        // when
        val result = mediator(networkDataSource).load(LoadType.REFRESH, emptyState())

        // then
        assertThat(result).isInstanceOf(RemoteMediator.MediatorResult.Error::class.java)
    }

    @Test
    fun shouldReturnSuccess_onAppend_whenNoRemoteKeyYet() = runTest {
        // given an empty paging state (refresh result not yet in the database)
        val networkDataSource = mock<LibraryNetworkDataSource>()

        // when
        val result = mediator(networkDataSource).load(LoadType.APPEND, emptyState())

        // then paging will retry once remote keys exist, so end is not reached
        assertThat(result).isInstanceOf(RemoteMediator.MediatorResult.Success::class.java)
        assertThat((result as RemoteMediator.MediatorResult.Success).endOfPaginationReached).isFalse()
    }
}
