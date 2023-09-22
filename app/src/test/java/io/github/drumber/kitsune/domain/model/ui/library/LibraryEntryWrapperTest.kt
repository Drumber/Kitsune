package io.github.drumber.kitsune.domain.model.ui.library

import io.github.drumber.kitsune.utils.libraryEntry
import io.github.drumber.kitsune.utils.libraryEntryModification
import net.datafaker.Faker
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class LibraryEntryWrapperTest {

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