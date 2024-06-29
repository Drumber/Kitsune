package io.github.drumber.kitsune.domain_old.mapper

import io.github.drumber.kitsune.testutils.anime
import io.github.drumber.kitsune.testutils.localAnime
import net.datafaker.Faker
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class AnimeMapperTest {

    private val faker = Faker()

    @Test
    fun shouldMapAnimeToLocalAnime() {
        // given
        val anime = anime(faker)

        // when
        val localAnime = anime.toLocalAnime()

        // then
        assertThat(localAnime).usingRecursiveComparison().isEqualTo(anime)
    }

    @Test
    fun shouldMapLocalAnimeToAnime() {
        // given
        val localAnime = localAnime(faker)

        // when
        val anime = localAnime.toAnime()

        // then
        assertThat(anime)
            .usingRecursiveComparison()
            .ignoringFields("streamingLinks", "animeProduction", "mediaRelationships", "categories")
            .isEqualTo(localAnime)
    }

}