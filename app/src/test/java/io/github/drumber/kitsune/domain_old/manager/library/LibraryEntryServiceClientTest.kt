package io.github.drumber.kitsune.domain_old.manager.library

import com.github.jasminb.jsonapi.JSONAPIDocument
import io.github.drumber.kitsune.domain_old.model.database.LocalLibraryEntryModification
import io.github.drumber.kitsune.domain_old.model.infrastructure.library.LibraryEntry
import io.github.drumber.kitsune.domain_old.model.common.library.LibraryStatus
import io.github.drumber.kitsune.domain_old.service.library.LibraryEntriesService
import io.github.drumber.kitsune.exception.NotFoundException
import io.github.drumber.kitsune.testutils.anime
import io.github.drumber.kitsune.testutils.libraryEntry
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import net.datafaker.Faker
import okhttp3.ResponseBody.Companion.toResponseBody
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.argThat
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import retrofit2.HttpException
import retrofit2.Response

class LibraryEntryServiceClientTest {

    private val faker = Faker()

    @Test
    fun shouldPostNewLibraryEntry() = runTest {
        // given
        val captor = argumentCaptor<JSONAPIDocument<LibraryEntry>>()
        val libraryEntriesService = mock<LibraryEntriesService> {
            on(it.postLibraryEntry(any(), any()))
                .thenReturn(JSONAPIDocument())
        }
        val client = LibraryEntryServiceClient(libraryEntriesService)
        val userId = faker.internet().uuid()
        val media = anime(faker)
        val status = LibraryStatus.Planned

        // when
        client.postNewLibraryEntry(userId, media, status)

        // then
        verify(libraryEntriesService).postLibraryEntry(captor.capture(), any())
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
            on(it.updateLibraryEntry(any(), any(), any()))
                .thenReturn(JSONAPIDocument())
        }
        val client = LibraryEntryServiceClient(libraryEntriesService)
        val modification = LocalLibraryEntryModification
            .withIdAndNulls(faker.internet().uuid())
            .copy(status = LibraryStatus.Completed)

        // when
        client.updateLibraryEntryWithModification(modification, true)

        // then
        verify(libraryEntriesService).updateLibraryEntry(eq(modification.id), captor.capture(), any())
        val libraryEntry = captor.lastValue.get()
        assertThat(libraryEntry).isNotNull
        assertThat(libraryEntry!!.status).isEqualTo(modification.status)
    }

    @Test
    fun shouldThrowNotFoundOnUpdateDeletedLibraryEntryWithModification() = runTest {
        // given
        val libraryEntriesService = mock<LibraryEntriesService> {
            on(it.updateLibraryEntry(any(), any(), any()))
                .thenThrow(
                    HttpException(
                        Response.error<JSONAPIDocument<LibraryEntry>>(
                            404,
                            "".toResponseBody()
                        )
                    )
                )
        }
        val client = LibraryEntryServiceClient(libraryEntriesService)
        val modification = LocalLibraryEntryModification
            .withIdAndNulls(faker.internet().uuid())
            .copy(status = LibraryStatus.Completed)

        // then
        assertThatThrownBy {
            // when
            runBlocking { client.updateLibraryEntryWithModification(modification, true) }
        }.isInstanceOf(NotFoundException::class.java)
        verify(libraryEntriesService).updateLibraryEntry(eq(modification.id), any(), any())
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