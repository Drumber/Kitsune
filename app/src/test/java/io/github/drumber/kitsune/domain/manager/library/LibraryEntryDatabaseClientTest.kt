package io.github.drumber.kitsune.domain.manager.library

import io.github.drumber.kitsune.domain.database.LibraryEntryDao
import io.github.drumber.kitsune.domain.database.LocalDatabase
import io.github.drumber.kitsune.utils.localLibraryEntry
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import net.datafaker.Faker
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify

class LibraryEntryDatabaseClientTest {

    private val faker = Faker()

    @Test
    fun shouldInsertLibraryEntry() = runTest {
        // given
        val libraryEntryDao = mock<LibraryEntryDao> {
            on(it.insertSingle(any())).thenReturn(Unit)
        }
        val database = mock<LocalDatabase> {
            on(it.libraryEntryDao()).thenReturn(libraryEntryDao)
        }
        val client = LibraryEntryDatabaseClient(database)
        val localLibraryEntry = localLibraryEntry(faker)

        // when
        client.insertLibraryEntry(localLibraryEntry)

        // then
        verify(libraryEntryDao).insertSingle(eq(localLibraryEntry))
    }

    @Test
    fun shouldNotInsertLibraryEntry() = runTest {
        // given
        val libraryEntryDao = mock<LibraryEntryDao> {
            on(it.insertSingle(any())).thenReturn(Unit)
        }
        val database = mock<LocalDatabase> {
            on(it.libraryEntryDao()).thenReturn(libraryEntryDao)
        }
        val client = LibraryEntryDatabaseClient(database)
        val localLibraryEntry = localLibraryEntry(faker).copy(anime = null, manga = null)

        // then
        assertThatThrownBy {
            runBlocking { client.insertLibraryEntry(localLibraryEntry) }
        }.isInstanceOf(IllegalArgumentException::class.java)
        verify(libraryEntryDao, never()).insertSingle(any())
    }
}