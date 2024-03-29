package io.github.drumber.kitsune.domain.model.database

import io.github.drumber.kitsune.testutils.localLibraryEntry
import io.github.drumber.kitsune.testutils.localLibraryEntryModification
import net.datafaker.Faker
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class LocalLibraryEntryModificationTest {

    private val faker = Faker()

    @Test
    fun shouldApplyModificationsToLibraryEntry() {
        // given
        val libraryEntry = localLibraryEntry(faker)
        val modification = localLibraryEntryModification(faker)
            .copy(id = libraryEntry.id)

        // when
        val libraryEntryWithModifications = modification.applyToLibraryEntry(libraryEntry)

        // then
        assertThat(libraryEntryWithModifications)
            .usingRecursiveComparison()
            .comparingOnlyFields("id", "anime", "manga")
            .isEqualTo(libraryEntry)
        assertThat(libraryEntryWithModifications)
            .usingRecursiveComparison()
            .ignoringFields("id", "anime", "manga")
            .isNotEqualTo(libraryEntry)
    }

    @Test
    fun shouldApplyModificationsToLibraryEntryIgnoringBlankNotes() {
        // given
        val libraryEntry = localLibraryEntry(faker).copy(notes = null)
        val modification = localLibraryEntryModification(faker)
            .copy(id = libraryEntry.id, notes = "")

        // when
        val libraryEntryWithModifications = modification.applyToLibraryEntry(libraryEntry, true)

        // then
        assertThat(libraryEntryWithModifications.notes).isNull()
    }

    @Test
    fun shouldApplyModificationsToLibraryEntryWithoutIgnoringBlankNotes() {
        // given
        val libraryEntry = localLibraryEntry(faker).copy(notes = null)
        val modification = localLibraryEntryModification(faker)
            .copy(id = libraryEntry.id, notes = "")

        // when
        val libraryEntryWithModifications = modification.applyToLibraryEntry(libraryEntry, false)

        // then
        assertThat(libraryEntryWithModifications.notes).isEqualTo("")
    }

    @Test
    fun shouldCreateLibraryEntryFromModification() {
        // given
        val modification = localLibraryEntryModification(faker)

        // when
        val libraryEntry = modification.toLocalLibraryEntry()

        // then
        assertThat(libraryEntry)
            .usingRecursiveComparison()
            .ignoringFields("progressedAt", "reactionSkipped", "manga", "anime", "updatedAt", "reconsuming")
            .isEqualTo(modification)
    }

    @Test
    fun shouldBeEqualToLibraryEntry() {
        // given
        val libraryEntry = localLibraryEntry(faker)
        val modification = LocalLibraryEntryModification
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
        val libraryEntry = localLibraryEntry(faker)
        val modification = LocalLibraryEntryModification
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