package io.github.drumber.kitsune.data.mapper

import io.github.drumber.kitsune.data.mapper.ImageMapper.toImage
import io.github.drumber.kitsune.data.mapper.ReactionMapper.toMediaReaction
import io.github.drumber.kitsune.data.source.network.reaction.model.NetworkMediaReaction
import io.github.drumber.kitsune.testutils.networkAnime
import io.github.drumber.kitsune.testutils.networkManga
import io.github.drumber.kitsune.testutils.networkUser
import net.datafaker.Faker
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.Test

class ReactionMapperTest {

    private val faker = Faker()

    @Test
    fun shouldMap_NetworkMediaReaction_withAnime() {
        // given
        val user = networkUser(faker)
        val anime = networkAnime(faker)
        val networkReaction = NetworkMediaReaction(
            id = "11",
            reaction = "Loved it",
            content = "content",
            contentFormatted = "formatted",
            upVotesCount = 8,
            createdAt = "2024-01-01T00:00:00.000Z",
            user = user,
            anime = anime
        )

        // when
        val reaction = networkReaction.toMediaReaction()

        // then
        assertThat(reaction.id).isEqualTo("11")
        assertThat(reaction.reaction).isEqualTo("Loved it")
        assertThat(reaction.content).isEqualTo("content")
        assertThat(reaction.contentFormatted).isEqualTo("formatted")
        assertThat(reaction.upVotesCount).isEqualTo(8)
        assertThat(reaction.createdAt).isEqualTo("2024-01-01T00:00:00.000Z")
        assertThat(reaction.authorId).isEqualTo(user.id)
        assertThat(reaction.authorName).isEqualTo(user.name)
        assertThat(reaction.authorAvatarUrl).isEqualTo(user.avatar?.toImage()?.largeOrDown())
        assertThat(reaction.mediaId).isEqualTo(anime.id)
        assertThat(reaction.mediaTitle).isEqualTo(anime.canonicalTitle)
        assertThat(reaction.mediaPosterUrl).isEqualTo(anime.posterImage?.toImage()?.smallOrHigher())
        assertThat(reaction.mediaSlug).isEqualTo(anime.slug)
        assertThat(reaction.mediaIsAnime).isTrue()
    }

    @Test
    fun shouldMap_NetworkMediaReaction_withManga() {
        // given
        val manga = networkManga(faker)
        val networkReaction = NetworkMediaReaction(id = "1", manga = manga)

        // when
        val reaction = networkReaction.toMediaReaction()

        // then
        assertThat(reaction.mediaId).isEqualTo(manga.id)
        assertThat(reaction.mediaIsAnime).isFalse()
    }

    @Test
    fun shouldApplyDefaults_whenNullableFieldsAreNull() {
        // given
        val networkReaction = NetworkMediaReaction(id = "1")

        // when
        val reaction = networkReaction.toMediaReaction()

        // then
        assertThat(reaction.upVotesCount).isEqualTo(0)
        assertThat(reaction.authorId).isNull()
        assertThat(reaction.mediaId).isNull()
        assertThat(reaction.mediaIsAnime).isNull()
    }

    @Test
    fun shouldThrow_whenIdIsNull() {
        // given
        val networkReaction = NetworkMediaReaction(id = null)

        // expect
        assertThatThrownBy { networkReaction.toMediaReaction() }
            .isInstanceOf(MappingException::class.java)
    }
}
