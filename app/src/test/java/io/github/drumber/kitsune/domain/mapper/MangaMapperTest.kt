package io.github.drumber.kitsune.domain.mapper

import io.github.drumber.kitsune.testutils.localManga
import io.github.drumber.kitsune.testutils.manga
import net.datafaker.Faker
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class MangaMapperTest {

    private val faker = Faker()

    @Test
    fun shouldMapMangaToLocalManga() {
        // given
        val manga = manga(faker)

        // when
        val localManga = manga.toLocalManga()

        // then
        assertThat(localManga).usingRecursiveComparison().isEqualTo(manga)
    }

    @Test
    fun shouldMapLocalMangaToManga() {
        // given
        val localManga = localManga(faker)

        // when
        val manga = localManga.toManga()

        // then
        assertThat(manga)
            .usingRecursiveComparison()
            .ignoringFields("mediaRelationships", "categories")
            .isEqualTo(localManga)
    }

}