package io.github.drumber.kitsune.domain.manager.library

import io.github.drumber.kitsune.domain.mapper.toLocalLibraryEntry
import io.github.drumber.kitsune.domain.model.database.LocalLibraryEntryModification
import io.github.drumber.kitsune.domain.model.infrastructure.library.LibraryStatus
import io.github.drumber.kitsune.utils.anime
import io.github.drumber.kitsune.utils.libraryEntry
import io.github.drumber.kitsune.utils.localLibraryEntry
import kotlinx.coroutines.test.runTest
import net.datafaker.Faker
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify

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
        val manager = LibraryManager(databaseClient, serviceClient) { true }
        val userId = faker.internet().uuid()
        val media = anime(faker)
        val status = LibraryStatus.Planned

        // when
        val isSuccess = manager.addNewLibraryEntry(userId, media, status)

        // then
        verify(serviceClient).postNewLibraryEntry(userId, media, status)
        verify(databaseClient).insertLibraryEntry(libraryEntry.toLocalLibraryEntry())
        assertThat(isSuccess).isTrue
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
        val manager = LibraryManager(databaseClient, serviceClient) { true }
        val userId = faker.internet().uuid()
        val media = anime(faker)
        val status = LibraryStatus.Planned

        // when
        val isSuccess = manager.addNewLibraryEntry(userId, media, status)

        // then
        verify(serviceClient).postNewLibraryEntry(userId, media, status)
        verify(databaseClient, never()).insertLibraryEntry(any())
        assertThat(isSuccess).isFalse
    }

    @Test
    fun shouldRemoveLibraryEntry() = runTest {
        // given
        val libraryEntry = localLibraryEntry(faker)
        val serviceClient = mock<LibraryEntryServiceClient> {
            on(it.deleteLibraryEntry(any())).thenReturn(true)
        }
        val databaseClient = mock<LibraryEntryDatabaseClient> {
            on(it.deleteLibraryEntryAndAnyModification(any())).thenReturn(Unit)
        }
        val manager = LibraryManager(databaseClient, serviceClient) { true }

        // when
        val isDeleted = manager.removeLibraryEntry(libraryEntry)

        // then
        verify(serviceClient).deleteLibraryEntry(libraryEntry.id)
        verify(databaseClient).deleteLibraryEntryAndAnyModification(libraryEntry)
        assertThat(isDeleted).isTrue
    }

    @Test
    fun shouldNotRemoveLibraryEntry() = runTest {
        // given
        val libraryEntry = localLibraryEntry(faker)
        val serviceClient = mock<LibraryEntryServiceClient> {
            on(it.deleteLibraryEntry(any())).thenReturn(false)
        }
        val databaseClient = mock<LibraryEntryDatabaseClient> {
            on(it.deleteLibraryEntryAndAnyModification(any())).thenReturn(Unit)
        }
        val manager = LibraryManager(databaseClient, serviceClient) { true }

        // when
        val isDeleted = manager.removeLibraryEntry(libraryEntry)

        // then
        verify(serviceClient).deleteLibraryEntry(libraryEntry.id)
        verify(databaseClient, never()).insertLibraryEntry(any())
        assertThat(isDeleted).isFalse
    }


    @Test
    fun shouldMayRemoveLibraryEntryLocally() = runTest {
        // given
        val libraryEntry = localLibraryEntry(faker)
        val serviceClient = mock<LibraryEntryServiceClient> {
            on(it.doesLibraryEntryExist(any())).thenReturn(false)
        }
        val databaseClient = mock<LibraryEntryDatabaseClient> {
            on(it.deleteLibraryEntryAndAnyModification(any())).thenReturn(Unit)
        }
        val manager = LibraryManager(databaseClient, serviceClient) { true }

        // when
        manager.mayRemoveLibraryEntryLocally(libraryEntry)

        // then
        verify(serviceClient).doesLibraryEntryExist(libraryEntry.id)
        verify(databaseClient).deleteLibraryEntryAndAnyModification(libraryEntry)
    }

    @Test
    fun shouldNotMayRemoveLibraryEntryLocally() = runTest {
        // given
        val libraryEntry = localLibraryEntry(faker)
        val serviceClient = mock<LibraryEntryServiceClient> {
            on(it.doesLibraryEntryExist(any())).thenReturn(true)
        }
        val databaseClient = mock<LibraryEntryDatabaseClient> {
            on(it.deleteLibraryEntryAndAnyModification(any())).thenReturn(Unit)
        }
        val manager = LibraryManager(databaseClient, serviceClient) { true }

        // when
        manager.mayRemoveLibraryEntryLocally(libraryEntry)

        // then
        verify(serviceClient).doesLibraryEntryExist(libraryEntry.id)
        verify(databaseClient, never()).deleteLibraryEntryAndAnyModification(any())
    }

    @Test
    fun shouldSynchroniseLocalModificationWithService() = runTest {
        // given
        val libraryEntryModification = LocalLibraryEntryModification
            .withIdAndNulls(faker.internet().uuid())
            .copy(status = LibraryStatus.Completed)
        val expectedLibraryEntry = libraryEntry(faker)
            .copy(id = libraryEntryModification.id, status = libraryEntryModification.status)

        val serviceClient = mock<LibraryEntryServiceClient> {
            on(it.updateLibraryEntryWithModification(any())).thenReturn(expectedLibraryEntry)
        }
        val databaseClient = mock<LibraryEntryDatabaseClient> {
            on(it.insertLibraryEntryModification(any())).thenReturn(Unit)
            on(it.updateLibraryEntryAndDeleteModification(any(), any())).thenReturn(Unit)
        }
        val manager = LibraryManager(databaseClient, serviceClient) { true }

        // when
        val isSuccess = manager.synchronizeLocalModificationWithService(libraryEntryModification)

        // then
        verify(serviceClient).updateLibraryEntryWithModification(libraryEntryModification)
        verify(databaseClient).updateLibraryEntryAndDeleteModification(
            expectedLibraryEntry.toLocalLibraryEntry(),
            libraryEntryModification
        )
        verify(databaseClient, never()).insertLibraryEntryModification(any())
        assertThat(isSuccess).isTrue
    }

    @Test
    fun shouldNotSynchroniseLocalModificationWithService() = runTest {
        // given
        val libraryEntryModification = LocalLibraryEntryModification
            .withIdAndNulls(faker.internet().uuid())
            .copy(status = LibraryStatus.Completed)

        val serviceClient = mock<LibraryEntryServiceClient> {
            on(it.updateLibraryEntryWithModification(any())).thenReturn(null)
        }
        val databaseClient = mock<LibraryEntryDatabaseClient> {
            on(it.insertLibraryEntryModification(any())).thenReturn(Unit)
            on(it.updateLibraryEntryAndDeleteModification(any(), any())).thenReturn(Unit)
        }
        val manager = LibraryManager(databaseClient, serviceClient) { true }

        // when
        val isSuccess = manager.synchronizeLocalModificationWithService(libraryEntryModification)

        // then
        verify(serviceClient).updateLibraryEntryWithModification(libraryEntryModification)
        verify(databaseClient).insertLibraryEntryModification(any())
        verify(databaseClient, never()).updateLibraryEntryAndDeleteModification(any(), any())
        assertThat(isSuccess).isFalse
    }
}