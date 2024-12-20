package io.github.drumber.kitsune.data.repository

import io.github.drumber.kitsune.data.common.exception.NotFoundException
import io.github.drumber.kitsune.data.mapper.LibraryMapper.toLibraryEntry
import io.github.drumber.kitsune.data.mapper.LibraryMapper.toLocalLibraryEntry
import io.github.drumber.kitsune.data.mapper.LibraryMapper.toLocalLibraryEntryModification
import io.github.drumber.kitsune.data.mapper.LibraryMapper.toNetworkLibraryStatus
import io.github.drumber.kitsune.data.presentation.model.library.LibraryEntry
import io.github.drumber.kitsune.data.presentation.model.library.LibraryEntryModification
import io.github.drumber.kitsune.data.presentation.model.library.LibraryStatus
import io.github.drumber.kitsune.data.repository.library.LibraryChangeListener
import io.github.drumber.kitsune.data.repository.library.LibraryRepository
import io.github.drumber.kitsune.data.source.graphql.library.LibraryApolloDataSource
import io.github.drumber.kitsune.data.source.local.library.LibraryLocalDataSource
import io.github.drumber.kitsune.data.source.local.library.model.LocalLibraryEntry
import io.github.drumber.kitsune.data.source.local.library.model.LocalLibraryModificationState.NOT_SYNCHRONIZED
import io.github.drumber.kitsune.data.source.local.library.model.LocalLibraryModificationState.SYNCHRONIZING
import io.github.drumber.kitsune.data.source.network.library.LibraryNetworkDataSource
import io.github.drumber.kitsune.data.source.network.library.model.NetworkLibraryEntry
import io.github.drumber.kitsune.testutils.anime
import io.github.drumber.kitsune.testutils.assertThatThrownBy
import io.github.drumber.kitsune.testutils.localLibraryEntry
import io.github.drumber.kitsune.testutils.network.FakeHttpException
import io.github.drumber.kitsune.testutils.networkLibraryEntry
import io.github.drumber.kitsune.testutils.onSuspend
import io.github.drumber.kitsune.testutils.useMockedAndroidLogger
import io.github.drumber.kitsune.util.DATE_FORMAT_ISO
import io.github.drumber.kitsune.util.formatDate
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.plus
import kotlinx.coroutines.test.runTest
import net.datafaker.Faker
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.doAnswer
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import java.io.IOException
import java.time.Instant
import java.util.Date

class LibraryRepositoryTest {

    private val faker = Faker()

    @Test
    fun shouldAddNewLibraryEntry() = runTest {
        // given
        val userId = faker.internet().uuid()
        val media = anime(faker)
        val status = LibraryStatus.Planned
        val libraryEntry = networkLibraryEntry(faker)

        val remoteDataSource = mock<LibraryNetworkDataSource> {
            onSuspend { postLibraryEntry(any(), any()) } doReturn libraryEntry
        }
        val localDataSource = mock<LibraryLocalDataSource> {
            onSuspend { insertLibraryEntry(any()) } doReturn Unit
        }

        val libraryRepository = LibraryRepository(
            remoteDataSource,
            mock<LibraryApolloDataSource>(),
            localDataSource,
            NoOpLibraryChangeListener,
            backgroundScope
        )

        // when
        val newLibraryEntry = libraryRepository.addNewLibraryEntry(userId, media, status)

        // then
        val networkLibraryEntryArg = argumentCaptor<NetworkLibraryEntry>()
        val localLibraryEntryArg = argumentCaptor<LocalLibraryEntry>()
        verify(remoteDataSource).postLibraryEntry(networkLibraryEntryArg.capture(), any())
        verify(localDataSource).insertLibraryEntry(localLibraryEntryArg.capture())

        assertThat(networkLibraryEntryArg.firstValue.user?.id).isEqualTo(userId)
        assertThat(networkLibraryEntryArg.firstValue.anime?.id).isEqualTo(media.id)
        assertThat(networkLibraryEntryArg.firstValue.status).isEqualTo(status.toNetworkLibraryStatus())

        assertThat(localLibraryEntryArg.firstValue.id).isEqualTo(libraryEntry.id)

        assertThat(newLibraryEntry).isNotNull
        assertThat(newLibraryEntry?.id).isEqualTo(libraryEntry.id)
    }

    @Test
    fun shouldNotAddNewLibraryEntryOnNetworkError() = runTest {
        // given
        val userId = faker.internet().uuid()
        val media = anime(faker)
        val status = LibraryStatus.Planned

        val remoteDataSource = mock<LibraryNetworkDataSource> {
            onSuspend { postLibraryEntry(any(), any()) } doAnswer { throw IOException() }
        }
        val localDataSource = mock<LibraryLocalDataSource> {
            onSuspend { insertLibraryEntry(any()) } doReturn Unit
        }

        val libraryRepository = LibraryRepository(
            remoteDataSource,
            mock<LibraryApolloDataSource>(),
            localDataSource,
            NoOpLibraryChangeListener,
            backgroundScope + SupervisorJob()
        )

        // then
        assertThatThrownBy {
            // when
            libraryRepository.addNewLibraryEntry(userId, media, status)
        }.isInstanceOf(IOException::class.java)

        verify(remoteDataSource).postLibraryEntry(any(), any())
        verify(localDataSource, times(0)).insertLibraryEntry(any())
    }


    @Test
    fun shouldRemoveLibraryEntry() = runTest {
        // given
        val libraryEntryId = faker.internet().uuid()

        val remoteDataSource = mock<LibraryNetworkDataSource> {
            onSuspend { deleteLibraryEntry(any()) } doReturn Unit
        }
        val localDataSource = mock<LibraryLocalDataSource> {
            onSuspend { deleteLibraryEntryAndAnyModification(any()) } doReturn Unit
        }

        val libraryRepository = LibraryRepository(
            remoteDataSource,
            mock<LibraryApolloDataSource>(),
            localDataSource,
            NoOpLibraryChangeListener,
            backgroundScope
        )

        // when
        libraryRepository.removeLibraryEntry(libraryEntryId)

        // then
        verify(remoteDataSource).deleteLibraryEntry(libraryEntryId)
        verify(localDataSource).deleteLibraryEntryAndAnyModification(libraryEntryId)
    }

    @Test
    fun shouldNotRemoveLibraryEntry() = runTest {
        // given
        val libraryEntryId = faker.internet().uuid()

        val remoteDataSource = mock<LibraryNetworkDataSource> {
            onSuspend { deleteLibraryEntry(any()) } doAnswer { throw IOException() }
        }
        val localDataSource = mock<LibraryLocalDataSource> {
            onSuspend { deleteLibraryEntryAndAnyModification(any()) } doReturn Unit
        }

        val libraryRepository = LibraryRepository(
            remoteDataSource,
            mock<LibraryApolloDataSource>(),
            localDataSource,
            NoOpLibraryChangeListener,
            backgroundScope
        )

        // then
        assertThatThrownBy {
            // when
            libraryRepository.removeLibraryEntry(libraryEntryId)
        }.isInstanceOf(IOException::class.java)

        verify(remoteDataSource).deleteLibraryEntry(libraryEntryId)
        verify(localDataSource, never()).insertLibraryEntry(any())
    }

    @Test
    fun shouldMayRemoveLibraryEntryLocally() = runTest {
        // given
        val libraryEntryId = faker.internet().uuid()

        val remoteDataSource = mock<LibraryNetworkDataSource> {
            onSuspend { getLibraryEntry(any(), any()) } doAnswer { throw FakeHttpException(404) }
        }
        val localDataSource = mock<LibraryLocalDataSource> {
            onSuspend { deleteLibraryEntryAndAnyModification(any()) } doReturn Unit
        }

        val libraryRepository = LibraryRepository(
            remoteDataSource,
            mock<LibraryApolloDataSource>(),
            localDataSource,
            NoOpLibraryChangeListener,
            backgroundScope
        )

        // when
        libraryRepository.mayRemoveLibraryEntryLocally(libraryEntryId)

        // then
        verify(remoteDataSource).getLibraryEntry(eq(libraryEntryId), any())
        verify(localDataSource).deleteLibraryEntryAndAnyModification(libraryEntryId)
    }

    @Test
    fun shouldNotMayRemoveLibraryEntryLocally() = runTest {
        // given
        val libraryEntryId = faker.internet().uuid()

        val remoteDataSource = mock<LibraryNetworkDataSource> {
            onSuspend { getLibraryEntry(any(), any()) } doReturn networkLibraryEntry(faker)
        }
        val localDataSource = mock<LibraryLocalDataSource> {
            onSuspend { deleteLibraryEntryAndAnyModification(any()) } doReturn Unit
        }

        val libraryRepository = LibraryRepository(
            remoteDataSource,
            mock<LibraryApolloDataSource>(),
            localDataSource,
            NoOpLibraryChangeListener,
            backgroundScope
        )

        // when
        libraryRepository.mayRemoveLibraryEntryLocally(libraryEntryId)

        // then
        verify(remoteDataSource).getLibraryEntry(eq(libraryEntryId), any())
        verify(localDataSource, never()).deleteLibraryEntryAndAnyModification(any())
    }

    @Test
    fun shouldUpdateLibraryEntry() = runTest {
        // given
        val libraryEntryModification = LibraryEntryModification
            .withIdAndNulls(faker.internet().uuid())
            .copy(status = LibraryStatus.Completed)
        val expectedLibraryEntry = networkLibraryEntry(faker)
            .copy(
                id = libraryEntryModification.id,
                status = libraryEntryModification.status?.toNetworkLibraryStatus()
            )

        val remoteDataSource = mock<LibraryNetworkDataSource> {
            onSuspend { updateLibraryEntry(any(), any(), any()) } doReturn expectedLibraryEntry
        }
        val localDataSource = mock<LibraryLocalDataSource> {
            onSuspend { insertLibraryEntryModification(any()) } doReturn Unit
            onSuspend { updateLibraryEntryAndDeleteModification(any(), any()) } doReturn Unit
        }

        val libraryRepository = LibraryRepository(
            remoteDataSource,
            mock<LibraryApolloDataSource>(),
            localDataSource,
            NoOpLibraryChangeListener,
            backgroundScope
        )

        // when
        val result = libraryRepository.updateLibraryEntry(libraryEntryModification)

        // then
        verify(remoteDataSource).updateLibraryEntry(eq(libraryEntryModification.id), any(), any())
        verify(localDataSource).updateLibraryEntryAndDeleteModification(
            expectedLibraryEntry.toLocalLibraryEntry(),
            libraryEntryModification.toLocalLibraryEntryModification().copy(state = SYNCHRONIZING)
        )
        verify(localDataSource).insertLibraryEntryModification(
            libraryEntryModification.toLocalLibraryEntryModification().copy(state = SYNCHRONIZING)
        )
        assertThat(result).isEqualTo(expectedLibraryEntry.toLibraryEntry())
    }

    @Test
    fun shouldNotOverwriteMoreRecentChangesOnUpdateLibraryEntry() = runTest {
        // given
        val now = Instant.now()
        val libraryEntryModification = LibraryEntryModification
            .withIdAndNulls(faker.internet().uuid())

        val libraryEntryFromService = networkLibraryEntry(faker)
            .copy(updatedAt = Date.from(now).formatDate(DATE_FORMAT_ISO))

        val libraryEntryFromDb = localLibraryEntry(faker)
            .copy(updatedAt = Date.from(now.plusMillis(1)).formatDate(DATE_FORMAT_ISO))

        val remoteDataSource = mock<LibraryNetworkDataSource> {
            onSuspend { updateLibraryEntry(any(), any(), any()) } doReturn libraryEntryFromService
        }
        val localDataSource = mock<LibraryLocalDataSource> {
            onSuspend { insertLibraryEntryModification(any()) } doReturn Unit
            onSuspend { updateLibraryEntryAndDeleteModification(any(), any()) } doReturn Unit
            onSuspend { getLibraryEntry(any()) } doReturn libraryEntryFromDb
        }

        val libraryRepository = LibraryRepository(
            remoteDataSource,
            mock<LibraryApolloDataSource>(),
            localDataSource,
            NoOpLibraryChangeListener,
            backgroundScope
        )

        // when
        libraryRepository.updateLibraryEntry(libraryEntryModification)

        // then
        verify(localDataSource, never()).updateLibraryEntryAndDeleteModification(any(), any())
    }

    @Test
    fun shouldFailOnUpdateLibraryEntry() = runTest {
        // given
        val libraryEntryModification = LibraryEntryModification
            .withIdAndNulls(faker.internet().uuid())
            .copy(status = LibraryStatus.Completed)

        val remoteDataSource = mock<LibraryNetworkDataSource> {
            onSuspend { updateLibraryEntry(any(), any(), any()) } doAnswer { throw IOException() }
        }
        val localDataSource = mock<LibraryLocalDataSource> {
            onSuspend { insertLibraryEntryModification(any()) } doReturn Unit
            onSuspend { updateLibraryEntryAndDeleteModification(any(), any()) } doReturn Unit
            onSuspend { deleteLibraryEntryAndAnyModification(any()) } doReturn Unit
        }

        val libraryRepository = LibraryRepository(
            remoteDataSource,
            mock<LibraryApolloDataSource>(),
            localDataSource,
            NoOpLibraryChangeListener,
            backgroundScope + SupervisorJob()
        )

        // then
        assertThatThrownBy {
            // when
            useMockedAndroidLogger {
                libraryRepository.updateLibraryEntry(libraryEntryModification)
            }
        }

        verify(remoteDataSource).updateLibraryEntry(eq(libraryEntryModification.id), any(), any())
        verify(localDataSource).insertLibraryEntryModification(
            libraryEntryModification.toLocalLibraryEntryModification()
                .copy(state = NOT_SYNCHRONIZED)
        )
        verify(localDataSource, never()).updateLibraryEntryAndDeleteModification(any(), any())
        verify(localDataSource, never()).deleteLibraryEntryAndAnyModification(any())
    }

    @Test
    fun shouldDeleteOnUpdateLibraryEntryWithNotFoundResponse() = runTest {
        // given
        val libraryEntryModification = LibraryEntryModification
            .withIdAndNulls(faker.internet().uuid())
            .copy(status = LibraryStatus.Completed)

        val remoteDataSource = mock<LibraryNetworkDataSource> {
            onSuspend {
                updateLibraryEntry(
                    any(),
                    any(),
                    any()
                )
            } doAnswer { throw FakeHttpException(404) }
        }
        val localDataSource = mock<LibraryLocalDataSource> {
            onSuspend { insertLibraryEntryModification(any()) } doReturn Unit
            onSuspend { updateLibraryEntryAndDeleteModification(any(), any()) } doReturn Unit
            onSuspend { deleteLibraryEntryAndAnyModification(any()) } doReturn Unit
        }

        val libraryRepository = LibraryRepository(
            remoteDataSource,
            mock<LibraryApolloDataSource>(),
            localDataSource,
            NoOpLibraryChangeListener,
            backgroundScope + SupervisorJob()
        )

        // then
        assertThatThrownBy {
            // when
            libraryRepository.updateLibraryEntry(libraryEntryModification)
        }.isInstanceOf(NotFoundException::class.java)

        verify(remoteDataSource).updateLibraryEntry(eq(libraryEntryModification.id), any(), any())
        verify(localDataSource).insertLibraryEntryModification(
            libraryEntryModification.toLocalLibraryEntryModification()
                .copy(state = SYNCHRONIZING)
        )
        verify(localDataSource, never()).updateLibraryEntryAndDeleteModification(any(), any())
        verify(localDataSource).deleteLibraryEntryAndAnyModification(libraryEntryModification.id)
    }


    object NoOpLibraryChangeListener : LibraryChangeListener {
        override fun onNewLibraryEntry(libraryEntry: LibraryEntry) {}

        override fun onUpdateLibraryEntry(
            libraryEntryModification: LibraryEntryModification,
            updatedLibraryEntry: LibraryEntry?
        ) {
        }

        override fun onRemoveLibraryEntry(id: String) {}

        override fun onDataInsertion(libraryEntries: List<LibraryEntry>) {}
    }
}