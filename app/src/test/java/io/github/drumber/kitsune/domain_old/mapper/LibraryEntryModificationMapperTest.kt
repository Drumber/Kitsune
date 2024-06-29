package io.github.drumber.kitsune.domain_old.mapper

import io.github.drumber.kitsune.testutils.libraryEntryModification
import io.github.drumber.kitsune.testutils.localLibraryEntryModification
import net.datafaker.Faker
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class LibraryEntryModificationMapperTest {

    private val faker = Faker()

    @Test
    fun shouldMapLibraryEntryModificationToLocalLibraryEntryModification() {
        // given
        val libraryEntryModification = libraryEntryModification(faker)

        // when
        val localLibraryEntryModification = libraryEntryModification.toLocalLibraryEntryModification()

        // then
        assertThat(localLibraryEntryModification)
            .usingRecursiveComparison()
            .ignoringFields("state", "createTime")
            .isEqualTo(libraryEntryModification)
    }

    @Test
    fun shouldMapLocalLibraryEntryModificationToLibraryEntryModification() {
        // given
        val localLibraryEntryModification = localLibraryEntryModification(faker)

        // when
        val libraryEntryModification = localLibraryEntryModification.toLibraryEntryModification()

        // then
        assertThat(libraryEntryModification)
            .usingRecursiveComparison()
            .ignoringFields("state")
            .isEqualTo(localLibraryEntryModification)
    }
}