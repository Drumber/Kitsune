package io.github.drumber.kitsune.domain_old.model.ui.library

import io.github.drumber.kitsune.data.presentation.model.library.LibraryEntryWrapper
import io.github.drumber.kitsune.testutils.libraryEntry
import io.github.drumber.kitsune.testutils.libraryEntryModification
import net.datafaker.Faker
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class NetworkLibraryEntryWrapperTest {

    private val faker = Faker()

    @Test
    fun shouldIsNotSyncedReturnTrue() {
        // given
        val wrapper = LibraryEntryWrapper(
            libraryEntry(faker),
            libraryEntryModification(faker),
            false
        )

        // when
        val isNotSynced = wrapper.isNotSynced

        // then
        assertThat(isNotSynced).isTrue
    }

    @Test
    fun shouldIsNotSyncedReturnFalseWhenEqual() {
        // given
        val libraryEntry = libraryEntry(faker)
        val wrapper = LibraryEntryWrapper(
            libraryEntry,
            LibraryEntryModification
                .withIdAndNulls(libraryEntry.id!!)
                .copy(progress = libraryEntry.progress),
            false
        )

        // when
        val isNotSynced = wrapper.isNotSynced

        // then
        assertThat(isNotSynced).isFalse
    }

    @Test
    fun shouldIsNotSyncedReturnFalseWhenSynchronizing() {
        // given
        val wrapper = LibraryEntryWrapper(
            libraryEntry(faker),
            libraryEntryModification(faker),
            true
        )

        // when
        val isNotSynced = wrapper.isNotSynced

        // then
        assertThat(isNotSynced).isFalse
    }

}