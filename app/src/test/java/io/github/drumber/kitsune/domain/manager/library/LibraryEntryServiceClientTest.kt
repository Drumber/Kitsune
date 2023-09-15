package io.github.drumber.kitsune.domain.manager.library

import com.github.jasminb.jsonapi.JSONAPIDocument
import io.github.drumber.kitsune.domain.model.database.LocalLibraryEntryModification
import io.github.drumber.kitsune.domain.model.infrastructure.library.LibraryEntry
import io.github.drumber.kitsune.domain.model.infrastructure.library.LibraryStatus
import io.github.drumber.kitsune.domain.service.library.LibraryEntriesService
import io.github.drumber.kitsune.utils.anime
import io.github.drumber.kitsune.utils.libraryEntry
import kotlinx.coroutines.test.runTest
import net.datafaker.Faker
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.argThat
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import retrofit2.Response

class LibraryEntryServiceClientTest {

    private val faker = Faker()

    @Test
    fun shouldPostNewLibraryEntry() = runTest {
        // given
        val captor = argumentCaptor<JSONAPIDocument<LibraryEntry>>()
        val libraryEntriesService = mock<LibraryEntriesService> {
            on(it.postLibraryEntry(any()))
                .thenReturn(JSONAPIDocument())
        }
        val client = LibraryEntryServiceClient(libraryEntriesService)
        val userId = faker.internet().uuid()
        val media = anime(faker)
        val status = LibraryStatus.Planned

        // when
        client.postNewLibraryEntry(userId, media, status)

        // then
        verify(libraryEntriesService).postLibraryEntry(captor.capture())
        val libraryEntry = captor.lastValue.get()
        assertThat(libraryEntry).isNotNull
        assertThat(libraryEntry!!.user!!.id).isEqualTo(userId)
        assertThat(libraryEntry.status).isEqualTo(status)
        assertThat(libraryEntry.anime!!.id).isEqualTo(media.id)
        assertThat(libraryEntry.manga).isNull()
    }

    @Test
    fun shouldUpdateLibraryEntryWithModification() = runTest {
        // given
        val captor = argumentCaptor<JSONAPIDocument<LibraryEntry>>()
        val libraryEntriesService = mock<LibraryEntriesService> {
            on(it.updateLibraryEntry(any(), any()))
                .thenReturn(JSONAPIDocument())
        }
        val client = LibraryEntryServiceClient(libraryEntriesService)
        val modification = LocalLibraryEntryModification
            .withIdAndNulls(faker.internet().uuid())
            .copy(status = LibraryStatus.Completed)

        // when
        client.updateLibraryEntryWithModification(modification)

        // then
        verify(libraryEntriesService).updateLibraryEntry(eq(modification.id), captor.capture())
        val libraryEntry = captor.lastValue.get()
        assertThat(libraryEntry).isNotNull
        assertThat(libraryEntry!!.status).isEqualTo(modification.status)
    }

    @Test
    fun shouldDeleteLibraryEntry() = runTest {
        // given
        val libraryEntriesService = mock<LibraryEntriesService> {
            on(it.deleteLibraryEntry(any()))
                .thenReturn(Response.success(any()))
        }
        val client = LibraryEntryServiceClient(libraryEntriesService)
        val id = faker.internet().uuid()

        // when
        val isSuccess = client.deleteLibraryEntry(id)

        // then
        verify(libraryEntriesService).deleteLibraryEntry(eq(id))
        assertThat(isSuccess).isTrue()
    }

    @Test
    fun shouldGetFullLibraryEntry() = runTest {
        // given
        val expectedLibraryEntry = libraryEntry(faker)
        val libraryEntriesService = mock<LibraryEntriesService> {
            on(it.getLibraryEntry(any(), any()))
                .thenReturn(JSONAPIDocument(expectedLibraryEntry))
        }
        val client = LibraryEntryServiceClient(libraryEntriesService)
        val id = faker.internet().uuid()

        // when
        val actualLibraryEntry = client.getFullLibraryEntry(id)

        // then
        verify(libraryEntriesService).getLibraryEntry(eq(id), argThat { contains("include") })
        assertThat(actualLibraryEntry).isEqualTo(expectedLibraryEntry)
    }
}