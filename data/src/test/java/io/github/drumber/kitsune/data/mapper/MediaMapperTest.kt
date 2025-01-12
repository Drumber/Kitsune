package io.github.drumber.kitsune.data.mapper

import io.github.drumber.kitsune.data.mapper.MediaMapper.toAnime
import io.github.drumber.kitsune.data.mapper.MediaMapper.toManga
import io.github.drumber.kitsune.data.mapper.MediaMapper.toMedia
import io.github.drumber.kitsune.data.model.media.Anime
import io.github.drumber.kitsune.data.model.media.Manga
import io.github.drumber.kitsune.data.model.media.Media
import io.github.drumber.kitsune.data.source.jsonapi.media.model.NetworkMedia
import io.github.drumber.kitsune.data.testutils.networkAnime
import io.github.drumber.kitsune.data.testutils.networkManga
import net.datafaker.Faker
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class MediaMapperTest {

    private val faker = Faker()

    @Test
    fun shouldMap_NetworkMedia_to_Media() {
        // given
        val networkAnime: NetworkMedia = networkAnime(faker)
        val networkManga: NetworkMedia = networkManga(faker)

        // when
        val anime: Media = networkAnime.toMedia()
        val manga: Media = networkManga.toMedia()

        // then
        assertThat(anime).isInstanceOf(Anime::class.java)
        assertThat(manga).isInstanceOf(Manga::class.java)
    }

    @Test
    fun shouldMap_NetworkAnime_to_Anime() {
        // given
        val networkAnime = networkAnime(faker)

        // when
        val anime = networkAnime.toAnime()

        // then
        assertThat(anime)
            .usingRecursiveComparison()
            .isEqualTo(networkAnime)
    }

    @Test
    fun shouldMap_NetworkManga_to_Manga() {
        // given
        val networkManga = networkManga(faker)

        // when
        val manga = networkManga.toManga()

        // then
        assertThat(manga)
            .usingRecursiveComparison()
            .isEqualTo(networkManga)
    }
}