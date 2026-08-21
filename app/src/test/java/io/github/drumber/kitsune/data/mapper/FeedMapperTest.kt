package io.github.drumber.kitsune.data.mapper

import io.github.drumber.kitsune.data.mapper.FeedMapper.toPost
import io.github.drumber.kitsune.data.mapper.ImageMapper.toImage
import io.github.drumber.kitsune.data.source.network.feed.model.NetworkPost
import io.github.drumber.kitsune.data.source.network.feed.model.NetworkUpload
import io.github.drumber.kitsune.testutils.networkAnime
import io.github.drumber.kitsune.testutils.networkImage
import io.github.drumber.kitsune.testutils.networkManga
import io.github.drumber.kitsune.testutils.networkUser
import io.github.drumber.kitsune.util.DataUtil
import net.datafaker.Faker
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.mockito.Mockito.mockStatic
import org.mockito.kotlin.any

class FeedMapperTest {

    private val faker = Faker()

    @Test
    fun shouldMap_NetworkPost_to_Post() {
        // given
        val user = networkUser(faker)
        val media = networkAnime(faker)
        val firstUpload = NetworkUpload(id = "u1", content = networkImage(faker), uploadOrder = 1)
        val secondUpload = NetworkUpload(id = "u2", content = networkImage(faker), uploadOrder = 0)
        val networkPost = NetworkPost(
            id = "42",
            createdAt = "2024-01-01T00:00:00.000Z",
            content = "content",
            contentFormatted = "formatted",
            spoiler = true,
            nsfw = true,
            commentsCount = 5,
            postLikesCount = 9,
            user = user,
            media = media,
            uploads = listOf(firstUpload, secondUpload)
        )

        // when
        val post = mockStatic(DataUtil::class.java).use {
            it.`when`<String> { DataUtil.getTitle(any(), any()) }.thenAnswer { invocation ->
                invocation.arguments[1]
            }

            networkPost.toPost()
        }

        // then
        assertThat(post.id).isEqualTo("42")
        assertThat(post.createdAt).isEqualTo("2024-01-01T00:00:00.000Z")
        assertThat(post.content).isEqualTo("content")
        assertThat(post.contentFormatted).isEqualTo("formatted")
        assertThat(post.spoiler).isTrue()
        assertThat(post.nsfw).isTrue()
        assertThat(post.commentsCount).isEqualTo(5)
        assertThat(post.likesCount).isEqualTo(9)
        assertThat(post.authorId).isEqualTo(user.id)
        assertThat(post.authorName).isEqualTo(user.name)
        assertThat(post.authorAvatarUrl).isEqualTo(user.avatar?.toImage()?.largeOrDown())
        assertThat(post.mediaTitle).isEqualTo(media.canonicalTitle)
        assertThat(post.mediaId).isEqualTo(media.id)
        assertThat(post.mediaPosterUrl).isEqualTo(media.posterImage?.toImage()?.smallOrHigher())
        assertThat(post.mediaSynopsis).isEqualTo(media.description)
        assertThat(post.mediaSlug).isEqualTo(media.slug)
        assertThat(post.mediaIsAnime).isTrue()
        // uploads are ordered by uploadOrder ascending
        assertThat(post.uploadIds).containsExactly("u2", "u1")
        assertThat(post.imageUrls).containsExactly(
            secondUpload.content?.toImage()?.largeOrDown(),
            firstUpload.content?.toImage()?.largeOrDown()
        )
    }

    @Test
    fun shouldApplyDefaults_whenNullableFieldsAreNull() {
        // given
        val networkPost = NetworkPost(id = "1")

        // when
        val post = networkPost.toPost()

        // then
        assertThat(post.spoiler).isFalse()
        assertThat(post.nsfw).isFalse()
        assertThat(post.commentsCount).isEqualTo(0)
        assertThat(post.likesCount).isEqualTo(0)
        assertThat(post.spoiledUnitIsEpisode).isFalse()
        assertThat(post.imageUrls).isEmpty()
        assertThat(post.uploadIds).isEmpty()
        assertThat(post.embed).isNull()
        assertThat(post.authorId).isNull()
        assertThat(post.mediaIsAnime).isNull()
    }

    @Test
    fun shouldMap_mediaIsAnime_falseForManga() {
        // given
        val networkPost = NetworkPost(id = "1", media = networkManga(faker))

        // when
        val post = mockStatic(DataUtil::class.java).use {
            it.`when`<String> { DataUtil.getTitle(any(), any()) }.thenReturn("title")

            networkPost.toPost()
        }

        // then
        assertThat(post.mediaIsAnime).isFalse()
    }

    @Test
    fun shouldThrow_whenIdIsNull() {
        // given
        val networkPost = NetworkPost(id = null)

        // expect
        org.assertj.core.api.Assertions.assertThatThrownBy { networkPost.toPost() }
            .isInstanceOf(MappingException::class.java)
    }
}
