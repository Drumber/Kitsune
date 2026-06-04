package io.github.drumber.kitsune.data.mapper

import com.fasterxml.jackson.databind.ObjectMapper
import io.github.drumber.kitsune.data.mapper.FeedMapper.toPost
import io.github.drumber.kitsune.data.source.network.feed.model.NetworkEmbed
import io.github.drumber.kitsune.data.source.network.feed.model.NetworkPost
import io.github.drumber.kitsune.data.source.network.feed.model.NetworkUpload
import io.github.drumber.kitsune.testutils.image
import io.github.drumber.kitsune.testutils.networkAnime
import io.github.drumber.kitsune.testutils.networkManga
import io.github.drumber.kitsune.testutils.networkUser
import net.datafaker.Faker
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class FeedMapperTest {

    private val faker = Faker()
    private val objectMapper = ObjectMapper()

    @Test
    fun shouldMap_NetworkPost_to_Post() {
        // given
        val user = networkUser(faker)
        val media = networkAnime(faker)
        val firstUpload = NetworkUpload(id = "u1", content = image(faker), uploadOrder = 1)
        val secondUpload = NetworkUpload(id = "u2", content = image(faker), uploadOrder = 0)
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
        val post = networkPost.toPost()

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
        assertThat(post.authorAvatarUrl).isEqualTo(user.avatar?.originalOrDown())
        assertThat(post.mediaTitle).isEqualTo(media.canonicalTitle)
        assertThat(post.mediaId).isEqualTo(media.id)
        assertThat(post.mediaPosterUrl).isEqualTo(media.posterImage?.originalOrDown())
        assertThat(post.mediaSynopsis).isEqualTo(media.description)
        assertThat(post.mediaSlug).isEqualTo(media.slug)
        assertThat(post.mediaIsAnime).isTrue()
        // uploads are ordered by uploadOrder ascending
        assertThat(post.uploadIds).containsExactly("u2", "u1")
        assertThat(post.imageUrls).containsExactly(
            secondUpload.content?.originalOrDown(),
            firstUpload.content?.originalOrDown()
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
        val post = networkPost.toPost()

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

    @Test
    fun shouldResolveEmbed_fromTextualNodes() {
        // given
        val embed = NetworkEmbed(
            kind = "link",
            title = "title",
            description = "desc",
            url = "https://example.com",
            site = objectMapper.valueToTree("Example"),
            image = objectMapper.valueToTree("https://example.com/image.png"),
            video = objectMapper.valueToTree("https://example.com/video.mp4")
        )

        // when
        val result = embed.toEmbed()

        // then
        assertThat(result.kind).isEqualTo("link")
        assertThat(result.title).isEqualTo("title")
        assertThat(result.description).isEqualTo("desc")
        assertThat(result.url).isEqualTo("https://example.com")
        assertThat(result.siteName).isEqualTo("Example")
        assertThat(result.imageUrl).isEqualTo("https://example.com/image.png")
        assertThat(result.videoUrl).isEqualTo("https://example.com/video.mp4")
        assertThat(result.videoType).isNull()
    }

    @Test
    fun shouldResolveEmbed_fromObjectNodes() {
        // given
        val embed = NetworkEmbed(
            site = objectMapper.valueToTree(mapOf("name" to "Tenor")),
            image = objectMapper.valueToTree(mapOf("url" to "https://example.com/i.gif")),
            video = objectMapper.valueToTree(
                mapOf("url" to "https://example.com/v.mp4", "type" to "video/mp4")
            )
        )

        // when
        val result = embed.toEmbed()

        // then
        assertThat(result.siteName).isEqualTo("Tenor")
        assertThat(result.imageUrl).isEqualTo("https://example.com/i.gif")
        assertThat(result.videoUrl).isEqualTo("https://example.com/v.mp4")
        assertThat(result.videoType).isEqualTo("video/mp4")
    }
}
