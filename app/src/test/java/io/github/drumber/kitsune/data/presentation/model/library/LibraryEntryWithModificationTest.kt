package io.github.drumber.kitsune.data.presentation.model.library

import io.github.drumber.kitsune.data.presentation.model.library.LibraryModificationState.SYNCHRONIZING
import io.github.drumber.kitsune.testutils.libraryEntryModification
import io.github.drumber.kitsune.testutils.libraryEntry
import net.datafaker.Faker
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class LibraryEntryWithModificationTest {

    private val faker = Faker()

    @Test
    fun shouldIsNotSyncedReturnTrue() {
        // given
        val wrapper = LibraryEntryWithModification(
            libraryEntry(faker),
            libraryEntryModification(faker)
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
        val wrapper = LibraryEntryWithModification(
            libraryEntry,
            LibraryEntryModification
                .withIdAndNulls(libraryEntry.id)
                .copy(progress = libraryEntry.progress)
        )

        // when
        val isNotSynced = wrapper.isNotSynced

        // then
        assertThat(isNotSynced).isFalse
    }

    @Test
    fun shouldIsNotSyncedReturnFalseWhenSynchronizing() {
        // given
        val wrapper = LibraryEntryWithModification(
            libraryEntry(faker),
            libraryEntryModification(faker).copy(state = SYNCHRONIZING)
        )

        // when
        val isNotSynced = wrapper.isNotSynced

        // then
        assertThat(isNotSynced).isFalse
    }
}