package io.github.drumber.kitsune.domain.library

import io.github.drumber.kitsune.data.presentation.model.library.LibraryEntryModification
import io.github.drumber.kitsune.data.presentation.model.library.LibraryStatus
import io.github.drumber.kitsune.data.repository.LibraryRepository
import io.github.drumber.kitsune.testutils.newLibraryEntry
import io.github.drumber.kitsune.testutils.onSuspend
import kotlinx.coroutines.test.runTest
import net.datafaker.Faker
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.times
import org.mockito.kotlin.verify

class SynchronizeLocalLibraryModificationsUseCaseTest {

    private val faker = Faker()

    @Test
    fun shouldSynchronizeAllLocalLibraryModifications() = runTest {
        // given
        val libraryEntryModifications = List(5) {
            LibraryEntryModification
                .withIdAndNulls(faker.internet().uuid())
                .copy(status = LibraryStatus.entries.random())
        }

        val libraryRepository = mock<LibraryRepository> {
            onSuspend { getAllLibraryEntryModifications() } doReturn libraryEntryModifications
        }
        val updateLibraryEntry = mock<UpdateLibraryEntryUseCase> {
            onSuspend { invoke(any()) } doReturn LibraryEntryUpdateResult.Success(
                newLibraryEntry(faker)
            )
        }

        val synchronizeLocalLibraryModifications =
            SynchronizeLocalLibraryModificationsUseCase(libraryRepository, updateLibraryEntry)

        // when
        val results = synchronizeLocalLibraryModifications()

        // then
        verify(libraryRepository).getAllLibraryEntryModifications()
        verify(updateLibraryEntry, times(libraryEntryModifications.size)).invoke(any())

        assertThat(results).containsOnlyKeys(libraryEntryModifications.map { it.id })
        assertThat(results).allSatisfy { _, synchronizationResult ->
            assertThat(synchronizationResult).isInstanceOf(LibraryEntryUpdateResult.Success::class.java)
        }
    }
}