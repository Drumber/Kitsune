package io.github.drumber.kitsune.data.model.library

import io.github.drumber.kitsune.data.model.testutils.libraryEntry
import io.github.drumber.kitsune.data.model.testutils.libraryEntryModification
import net.datafaker.Faker
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class LibraryEntryModificationTest {

    private val faker = Faker()

    @Test
    fun shouldApplyModificationsToLibraryEntry() {
        // given
        val libraryEntry = libraryEntry(faker)
        val modification = libraryEntryModification(faker)
            .copy(id = libraryEntry.id)

        // when
        val libraryEntryWithModifications = modification.applyToLibraryEntry(libraryEntry)

        // then
        assertThat(libraryEntryWithModifications)
            .usingRecursiveComparison()
            .comparingOnlyFields("id", "media")
            .isEqualTo(libraryEntry)
        assertThat(libraryEntryWithModifications)
            .usingRecursiveComparison()
            .ignoringFields("id", "media")
            .isNotEqualTo(libraryEntry)
    }

    @Test
    fun shouldApplyModificationsToLibraryEntryIgnoringBlankNotes() {
        // given
        val libraryEntry = libraryEntry(faker).copy(notes = null)
        val modification = libraryEntryModification(faker)
            .copy(id = libraryEntry.id, notes = "")

        // when
        val libraryEntryWithModifications = modification.applyToLibraryEntry(libraryEntry)

        // then
        assertThat(libraryEntryWithModifications.notes).isNull()
    }

    @Test
    fun shouldCreateLibraryEntryFromModification() {
        // given
        val modification = libraryEntryModification(faker)

        // when
        val libraryEntry = modification.toLocalLibraryEntry()

        // then
        assertThat(libraryEntry)
            .usingRecursiveComparison()
            .ignoringFields("progressedAt", "reactionSkipped", "media", "updatedAt", "reconsuming")
            .isEqualTo(modification)
    }

    @Test
    fun shouldBeEqualToLibraryEntry() {
        // given
        val libraryEntry = libraryEntry(faker)
        val modification = LibraryEntryModification
            .withIdAndNulls(faker.internet().uuid())
            .copy(
                progress = libraryEntry.progress,
                status = libraryEntry.status,
                ratingTwenty = libraryEntry.ratingTwenty
            )

        // when
        val isEqual = modification.isEqualToLibraryEntry(libraryEntry)

        // then
        assertThat(isEqual).isTrue
    }

    @Test
    fun shouldNotBeEqualToLibraryEntry() {
        // given
        val libraryEntry = libraryEntry(faker)
        val modification = LibraryEntryModification
            .withIdAndNulls(faker.internet().uuid())
            .copy(
                progress = libraryEntry.progress?.plus(1)
            )

        // when
        val isEqual = modification.isEqualToLibraryEntry(libraryEntry)

        // then
        assertThat(isEqual).isFalse
    }
}