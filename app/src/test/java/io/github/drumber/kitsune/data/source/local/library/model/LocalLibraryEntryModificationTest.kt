package io.github.drumber.kitsune.data.source.local.library.model

import io.github.drumber.kitsune.testutils.localLibraryEntry
import io.github.drumber.kitsune.util.DATE_FORMAT_ISO
import net.datafaker.Faker
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.Test

class LocalLibraryEntryModificationTest {

    private val faker = Faker()

    @Test
    fun shouldBeEqualToLibraryEntry() {
        // given
        val libraryEntry = localLibraryEntry(faker)

        val testCases = listOf(
            LocalLibraryEntryModification(
                id = libraryEntry.id,
                startedAt = libraryEntry.startedAt,
                finishedAt = libraryEntry.finishedAt,
                status = libraryEntry.status,
                progress = libraryEntry.progress,
                reconsumeCount = libraryEntry.reconsumeCount,
                volumesOwned = libraryEntry.volumesOwned,
                ratingTwenty = libraryEntry.ratingTwenty,
                notes = libraryEntry.notes,
                privateEntry = libraryEntry.privateEntry
            ),
            LocalLibraryEntryModification(
                id = libraryEntry.id,
                startedAt = null,
                finishedAt = null,
                status = null,
                progress = null,
                reconsumeCount = null,
                volumesOwned = null,
                ratingTwenty = null,
                notes = null,
                privateEntry = null
            ),
            LocalLibraryEntryModification(
                id = libraryEntry.id,
                startedAt = null,
                finishedAt = null,
                status = null,
                progress = libraryEntry.progress,
                reconsumeCount = null,
                volumesOwned = null,
                ratingTwenty = null,
                notes = null,
                privateEntry = null
            ),
            LocalLibraryEntryModification(
                id = libraryEntry.id,
                startedAt = null,
                finishedAt = null,
                status = null,
                progress = null,
                reconsumeCount = null,
                volumesOwned = null,
                ratingTwenty = null,
                notes = libraryEntry.notes,
                privateEntry = null
            )
        )

        // when & then
        testCases.forEach { libraryEntryModification ->
            val isEqual = libraryEntryModification.isEqualToLibraryEntry(libraryEntry)
            assertThat(isEqual).`as`("Modification $libraryEntryModification").isTrue()
        }
    }

    @Test
    fun shouldNotBeEqualToLibraryEntry() {
        // given
        val libraryEntry = localLibraryEntry(faker)

        val testCases = listOf(
            LocalLibraryEntryModification(
                id = libraryEntry.id,
                startedAt = libraryEntry.startedAt,
                finishedAt = libraryEntry.finishedAt,
                status = libraryEntry.status,
                progress = (libraryEntry.progress ?: 0) + 1,
                reconsumeCount = libraryEntry.reconsumeCount,
                volumesOwned = libraryEntry.volumesOwned,
                ratingTwenty = libraryEntry.ratingTwenty,
                notes = libraryEntry.notes,
                privateEntry = libraryEntry.privateEntry
            ),
            LocalLibraryEntryModification(
                id = libraryEntry.id,
                startedAt = null,
                finishedAt = null,
                status = null,
                progress = null,
                reconsumeCount = null,
                volumesOwned = null,
                ratingTwenty = null,
                notes = null,
                privateEntry = libraryEntry.privateEntry?.not() ?: true
            ),
            LocalLibraryEntryModification(
                id = "foo",
                startedAt = null,
                finishedAt = null,
                status = null,
                progress = null,
                reconsumeCount = null,
                volumesOwned = null,
                ratingTwenty = null,
                notes = null,
                privateEntry = null
            )
        )

        // when & then
        testCases.forEach { libraryEntryModification ->
            val isEqual = libraryEntryModification.isEqualToLibraryEntry(libraryEntry)
            assertThat(isEqual).`as`("Modification $libraryEntryModification").isFalse()
        }
    }

    @Test
    fun shouldApplyToLibraryEntry() {
        // given
        val libraryEntry = localLibraryEntry(faker)

        val modification = LocalLibraryEntryModification(
            id = libraryEntry.id,
            startedAt = faker.date().birthday(DATE_FORMAT_ISO),
            finishedAt = faker.date().birthday(DATE_FORMAT_ISO),
            status = LocalLibraryStatus.Completed,
            progress = 999,
            reconsumeCount = 123,
            volumesOwned = 456,
            ratingTwenty = 19,
            notes = "foo",
            privateEntry = true
        )

        // when
        val actualLibraryEntry = modification.applyToLibraryEntry(libraryEntry)

        // then
        assertThat(actualLibraryEntry)
            .usingRecursiveComparison()
            .isEqualTo(modification)
    }

    @Test
    fun shouldNotApplyBlankNotesToLibraryEntry() {
        // given
        val libraryEntry = localLibraryEntry(faker).copy(notes = null)

        val modification = LocalLibraryEntryModification(
            id = libraryEntry.id,
            startedAt = null,
            finishedAt = null,
            status = null,
            progress = null,
            reconsumeCount = null,
            volumesOwned = null,
            ratingTwenty = null,
            notes = "",
            privateEntry = null
        )

        // when
        val actualLibraryEntry = modification.applyToLibraryEntry(libraryEntry)

        // then
        assertThat(actualLibraryEntry.notes).isNull()
    }

    @Test
    fun shouldApplyBlankNotesToLibraryEntryIfItHasNotes() {
        // given
        val libraryEntry = localLibraryEntry(faker).copy(notes = "foo")

        val modification = LocalLibraryEntryModification(
            id = libraryEntry.id,
            startedAt = null,
            finishedAt = null,
            status = null,
            progress = null,
            reconsumeCount = null,
            volumesOwned = null,
            ratingTwenty = null,
            notes = "",
            privateEntry = null
        )

        // when
        val actualLibraryEntry = modification.applyToLibraryEntry(libraryEntry)

        // then
        assertThat(actualLibraryEntry.notes).isEqualTo("")
    }

    @Test
    fun shouldThrowIfApplyToLibraryEntryWithDifferentId() {
        // given
        val libraryEntry = localLibraryEntry(faker)

        val modification = LocalLibraryEntryModification(
            id = "foo",
            startedAt = faker.date().birthday(DATE_FORMAT_ISO),
            finishedAt = faker.date().birthday(DATE_FORMAT_ISO),
            status = LocalLibraryStatus.Completed,
            progress = 999,
            reconsumeCount = 123,
            volumesOwned = 456,
            ratingTwenty = 19,
            notes = "foo",
            privateEntry = true
        )

        // when
        assertThatThrownBy {
            // when
            modification.applyToLibraryEntry(libraryEntry)
        }.isInstanceOf(Exception::class.java)
    }
}