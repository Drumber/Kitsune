package io.github.drumber.kitsune.domain.manager.library

import io.github.drumber.kitsune.domain.mapper.toLocalLibraryEntry
import io.github.drumber.kitsune.domain.model.database.LocalLibraryEntryModification
import io.github.drumber.kitsune.domain.model.database.LocalLibraryModificationState.NOT_SYNCHRONIZED
import io.github.drumber.kitsune.domain.model.database.LocalLibraryModificationState.SYNCHRONIZING
import io.github.drumber.kitsune.domain.model.infrastructure.library.LibraryStatus
import io.github.drumber.kitsune.exception.NotFoundException
import io.github.drumber.kitsune.util.DATE_FORMAT_ISO
import io.github.drumber.kitsune.util.formatDate
import io.github.drumber.kitsune.utils.anime
import io.github.drumber.kitsune.utils.libraryEntry
import io.github.drumber.kitsune.utils.localLibraryEntry
import io.github.drumber.kitsune.utils.useMockedAndroidLogger
import kotlinx.coroutines.test.runTest
import net.datafaker.Faker
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.doAnswer
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.spy
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.time.Instant
import java.util.Date

class LibraryManagerTest {

    private val faker = Faker()

    @Test
    fun shouldAddNewLibraryEntry() = runTest {
        // given
        val libraryEntry = libraryEntry(faker)
        val serviceClient = mock<LibraryEntryServiceClient> {
            on(it.postNewLibraryEntry(any(), any(), any())).thenReturn(libraryEntry)
        }
        val databaseClient = mock<LibraryEntryDatabaseClient> {
            on(it.insertLibraryEntry(any())).thenReturn(Unit)
        }
        val manager = LibraryManager(databaseClient, serviceClient)
        val userId = faker.internet().uuid()
        val media = anime(faker)
        val status = LibraryStatus.Planned

        // when
        val newLibraryEntry = manager.addNewLibraryEntry(userId, media, status)

        // then
        verify(serviceClient).postNewLibraryEntry(userId, media, status)
        verify(databaseClient).insertLibraryEntry(libraryEntry.toLocalLibraryEntry())
        assertThat(newLibraryEntry).isNotNull
    }

    @Test
    fun shouldNotAddNewLibraryEntry() = runTest {
        // given
        val serviceClient = mock<LibraryEntryServiceClient> {
            on(it.postNewLibraryEntry(any(), any(), any())).thenReturn(null)
        }
        val databaseClient = mock<LibraryEntryDatabaseClient> {
            on(it.insertLibraryEntry(any())).thenReturn(Unit)
        }
        val manager = LibraryManager(databaseClient, serviceClient)
        val userId = faker.internet().uuid()
        val media = anime(faker)
        val status = LibraryStatus.Planned

        // when
        val newLibraryEntry = manager.addNewLibraryEntry(userId, media, status)

        // then
        verify(serviceClient).postNewLibraryEntry(userId, media, status)
        verify(databaseClient, never()).insertLibraryEntry(any())
        assertThat(newLibraryEntry).isNull()
    }

    @Test
    fun shouldRemoveLibraryEntry() = runTest {
        // given
        val libraryEntryId = faker.internet().uuid()
        val serviceClient = mock<LibraryEntryServiceClient> {
            on(it.deleteLibraryEntry(any())).thenReturn(true)
        }
        val databaseClient = mock<LibraryEntryDatabaseClient> {
            on(it.deleteLibraryEntryAndAnyModification(any())).thenReturn(Unit)
        }
        val manager = LibraryManager(databaseClient, serviceClient)

        // when
        val isDeleted = manager.removeLibraryEntry(libraryEntryId)

        // then
        verify(serviceClient).deleteLibraryEntry(libraryEntryId)
        verify(databaseClient).deleteLibraryEntryAndAnyModification(libraryEntryId)
        assertThat(isDeleted).isTrue
    }

    @Test
    fun shouldNotRemoveLibraryEntry() = runTest {
        // given
        val libraryEntryId = faker.internet().uuid()
        val serviceClient = mock<LibraryEntryServiceClient> {
            on(it.deleteLibraryEntry(any())).thenReturn(false)
        }
        val databaseClient = mock<LibraryEntryDatabaseClient> {
            on(it.deleteLibraryEntryAndAnyModification(any())).thenReturn(Unit)
        }
        val manager = LibraryManager(databaseClient, serviceClient)

        // when
        val isDeleted = manager.removeLibraryEntry(libraryEntryId)

        // then
        verify(serviceClient).deleteLibraryEntry(libraryEntryId)
        verify(databaseClient, never()).insertLibraryEntry(any())
        assertThat(isDeleted).isFalse
    }


    @Test
    fun shouldMayRemoveLibraryEntryLocally() = runTest {
        // given
        val libraryEntryId = faker.internet().uuid()
        val serviceClient = mock<LibraryEntryServiceClient> {
            on(it.doesLibraryEntryExist(any())).thenReturn(false)
        }
        val databaseClient = mock<LibraryEntryDatabaseClient> {
            on(it.deleteLibraryEntryAndAnyModification(any())).thenReturn(Unit)
        }
        val manager = LibraryManager(databaseClient, serviceClient)

        // when
        manager.mayRemoveLibraryEntryLocally(libraryEntryId)

        // then
        verify(serviceClient).doesLibraryEntryExist(libraryEntryId)
        verify(databaseClient).deleteLibraryEntryAndAnyModification(libraryEntryId)
    }

    @Test
    fun shouldNotMayRemoveLibraryEntryLocally() = runTest {
        // given
        val libraryEntryId = faker.internet().uuid()
        val serviceClient = mock<LibraryEntryServiceClient> {
            on(it.doesLibraryEntryExist(any())).thenReturn(true)
        }
        val databaseClient = mock<LibraryEntryDatabaseClient> {
            on(it.deleteLibraryEntryAndAnyModification(any())).thenReturn(Unit)
        }
        val manager = LibraryManager(databaseClient, serviceClient)

        // when
        manager.mayRemoveLibraryEntryLocally(libraryEntryId)

        // then
        verify(serviceClient).doesLibraryEntryExist(libraryEntryId)
        verify(databaseClient, never()).deleteLibraryEntryAndAnyModification(any())
    }

    @Test
    fun shouldUpdateLibraryEntry() = runTest {
        // given
        val libraryEntryModification = LocalLibraryEntryModification
            .withIdAndNulls(faker.internet().uuid())
            .copy(status = LibraryStatus.Completed)
        val expectedLibraryEntry = libraryEntry(faker)
            .copy(id = libraryEntryModification.id, status = libraryEntryModification.status)

        val serviceClient = mock<LibraryEntryServiceClient> {
            on(it.updateLibraryEntryWithModification(any(), any())).thenReturn(expectedLibraryEntry)
        }
        val databaseClient = mock<LibraryEntryDatabaseClient> {
            on(it.insertLibraryEntryModification(any())).thenReturn(Unit)
            on(it.updateLibraryEntryAndDeleteModification(any(), any())).thenReturn(Unit)
        }
        val manager = LibraryManager(databaseClient, serviceClient)

        // when
        val result = manager.updateLibraryEntry(libraryEntryModification)

        // then
        verify(serviceClient).updateLibraryEntryWithModification(
            libraryEntryModification.copy(state = SYNCHRONIZING),
            true
        )
        verify(databaseClient).updateLibraryEntryAndDeleteModification(
            expectedLibraryEntry.toLocalLibraryEntry(),
            libraryEntryModification.copy(state = SYNCHRONIZING)
        )
        verify(databaseClient).insertLibraryEntryModification(any())
        assertThat(result).isInstanceOf(SynchronizationResult.Success::class.java)
    }

    @Test
    fun shouldNotOverwriteMoreRecentChangesOnUpdateLibraryEntry() = runTest {
        // given
        val now = Instant.now()
        val libraryEntryModification = LocalLibraryEntryModification
            .withIdAndNulls(faker.internet().uuid())
        val libraryEntryFromService = libraryEntry(faker)
            .copy(updatedAt = Date.from(now).formatDate(DATE_FORMAT_ISO))
        val libraryEntryFromDb = localLibraryEntry(faker)
            .copy(updatedAt = Date.from(now.plusMillis(1)).formatDate(DATE_FORMAT_ISO))

        val serviceClient = mock<LibraryEntryServiceClient> {
            on(it.updateLibraryEntryWithModification(any(), any()))
                .thenReturn(libraryEntryFromService)
        }
        val databaseClient = mock<LibraryEntryDatabaseClient> {
            on(it.insertLibraryEntryModification(any())).thenReturn(Unit)
            on(it.updateLibraryEntryAndDeleteModification(any(), any())).thenReturn(Unit)
            on(it.getLibraryEntry(any())).thenReturn(libraryEntryFromDb)
        }
        val manager = LibraryManager(databaseClient, serviceClient)

        // when
        manager.updateLibraryEntry(libraryEntryModification)

        // then
        verify(databaseClient, never()).updateLibraryEntryAndDeleteModification(any(), any())
    }

    @Test
    fun shouldFailOnUpdateLibraryEntry() = runTest {
        // given
        val libraryEntryModification = LocalLibraryEntryModification
            .withIdAndNulls(faker.internet().uuid())
            .copy(status = LibraryStatus.Completed)

        val serviceClient = mock<LibraryEntryServiceClient> {
            on(it.updateLibraryEntryWithModification(any(), any())).thenReturn(null)
        }
        val databaseClient = mock<LibraryEntryDatabaseClient> {
            on(it.insertLibraryEntryModification(any())).thenReturn(Unit)
            on(it.updateLibraryEntryAndDeleteModification(any(), any())).thenReturn(Unit)
            on(it.deleteLibraryEntryAndAnyModification(any())).thenReturn(Unit)
        }
        val manager = LibraryManager(databaseClient, serviceClient)

        // when
        val result = useMockedAndroidLogger {
            manager.updateLibraryEntry(libraryEntryModification)
        }

        // then
        verify(serviceClient)
            .updateLibraryEntryWithModification(
                libraryEntryModification.copy(state = SYNCHRONIZING),
                true
            )
        verify(databaseClient)
            .insertLibraryEntryModification(libraryEntryModification.copy(state = NOT_SYNCHRONIZED))
        verify(databaseClient, never()).updateLibraryEntryAndDeleteModification(any(), any())
        verify(databaseClient, never()).deleteLibraryEntryAndAnyModification(any())
        assertThat(result).isInstanceOf(SynchronizationResult.Failed::class.java)
    }

    @Test
    fun shouldNotFoundOnUpdateLibraryEntry() = runTest {
        // given
        val libraryEntryModification = LocalLibraryEntryModification
            .withIdAndNulls(faker.internet().uuid())
            .copy(status = LibraryStatus.Completed)

        val serviceClient = mock<LibraryEntryServiceClient> {
            on(it.updateLibraryEntryWithModification(any(), any()))
                .doAnswer { throw NotFoundException() }
        }
        val databaseClient = mock<LibraryEntryDatabaseClient> {
            on(it.insertLibraryEntryModification(any())).thenReturn(Unit)
            on(it.updateLibraryEntryAndDeleteModification(any(), any())).thenReturn(Unit)
        }
        val manager = LibraryManager(databaseClient, serviceClient)

        // when
        val result = useMockedAndroidLogger {
            manager.updateLibraryEntry(libraryEntryModification)
        }

        // then
        verify(serviceClient)
            .updateLibraryEntryWithModification(
                libraryEntryModification.copy(state = SYNCHRONIZING),
                true
            )
        verify(databaseClient)
            .insertLibraryEntryModification(libraryEntryModification.copy(state = SYNCHRONIZING))
        verify(databaseClient, never()).updateLibraryEntryAndDeleteModification(any(), any())
        verify(databaseClient).deleteLibraryEntryAndAnyModification(libraryEntryModification.id)
        assertThat(result).isInstanceOf(SynchronizationResult.NotFound::class.java)
    }

    @Test
    fun shouldPushAllStoredLocalModificationsToService() = runTest {
        // given
        val libraryEntryModifications = List(5) {
            LocalLibraryEntryModification
                .withIdAndNulls(faker.internet().uuid())
                .copy(status = LibraryStatus.values().random())
        }

        val serviceClient = mock<LibraryEntryServiceClient>()
        val databaseClient = mock<LibraryEntryDatabaseClient> {
            on(it.getAllLocalLibraryModifications()).thenReturn(libraryEntryModifications)
        }
        val manager = spy(LibraryManager(databaseClient, serviceClient)) {
            doReturn(SynchronizationResult.Success(libraryEntry(faker)))
                .whenever(it).updateLibraryEntry(any<LocalLibraryEntryModification>())
        }

        // when
        val results = manager.pushAllStoredLocalModificationsToService()

        // then
        verify(
            manager,
            times(libraryEntryModifications.size)
        ).updateLibraryEntry(any<LocalLibraryEntryModification>())
        assertThat(results).containsOnlyKeys(libraryEntryModifications.map { it.id })
        assertThat(results).allSatisfy { _, synchronizationResult ->
            assertThat(synchronizationResult).isInstanceOf(SynchronizationResult.Success::class.java)
        }
    }

}