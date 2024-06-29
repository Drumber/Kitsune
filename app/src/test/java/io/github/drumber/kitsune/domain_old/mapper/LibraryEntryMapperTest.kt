package io.github.drumber.kitsune.domain_old.mapper

import io.github.drumber.kitsune.testutils.libraryEntry
import io.github.drumber.kitsune.testutils.localLibraryEntry
import net.datafaker.Faker
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class LibraryEntryMapperTest {

    private val faker = Faker()

    @Test
    fun shouldMapLibraryEntryToLocalLibraryEntryWithAnime() {
        // given
        val libraryEntry = libraryEntry(faker, true)

        // when
        val localLibraryEntry = libraryEntry.toLocalLibraryEntry()

        // then
        assertThat(localLibraryEntry).usingRecursiveComparison().isEqualTo(libraryEntry)
    }

    @Test
    fun shouldMapLibraryEntryToLocalLibraryEntryWithManga() {
        // given
        val libraryEntry = libraryEntry(faker, false)

        // when
        val localLibraryEntry = libraryEntry.toLocalLibraryEntry()

        // then
        assertThat(localLibraryEntry).usingRecursiveComparison().isEqualTo(libraryEntry)
    }

    @Test
    fun shouldMapLocalLibraryEntryToLibraryEntryWithAnime() {
        // given
        val localLibraryEntry = localLibraryEntry(faker, true)

        // when
        val libraryEntry = localLibraryEntry.toLibraryEntry()

        // then
        assertThat(libraryEntry)
            .usingRecursiveComparison()
            .ignoringFields(
                "user",
                "anime.streamingLinks",
                "anime.animeProduction",
                "anime.mediaRelationships",
                "anime.categories",
                "manga.mediaRelationships",
                "manga.categories"
            )
            .isEqualTo(localLibraryEntry)
    }

    @Test
    fun shouldMapLocalLibraryEntryToLibraryEntry() {
        // given
        val localLibraryEntry = localLibraryEntry(faker, false)

        // when
        val libraryEntry = localLibraryEntry.toLibraryEntry()

        // then
        assertThat(libraryEntry)
            .usingRecursiveComparison()
            .ignoringFields(
                "user",
                "anime.streamingLinks",
                "anime.animeProduction",
                "anime.mediaRelationships",
                "anime.categories",
                "manga.mediaRelationships",
                "manga.categories"
            )
            .isEqualTo(localLibraryEntry)
    }

}