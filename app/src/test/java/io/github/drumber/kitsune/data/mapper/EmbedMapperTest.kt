package io.github.drumber.kitsune.data.mapper

import com.fasterxml.jackson.databind.ObjectMapper
import io.github.drumber.kitsune.data.mapper.EmbedMapper.toEmbed
import io.github.drumber.kitsune.data.source.network.feed.model.NetworkEmbed
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class EmbedMapperTest {

    private val objectMapper = ObjectMapper()

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

    @Test
    fun shouldResolveNullSiteName_whenNameFieldIsJsonNull() {
        // given
        val embed = NetworkEmbed(
            kind = "link",
            title = "title",
            site = objectMapper.readTree("""{"name": null}"""),
            image = objectMapper.readTree("""{"url": null}"""),
            video = objectMapper.readTree("""{"url": null, "type": null}""")
        )

        // when
        val result = embed.toEmbed()

        // then
        assertThat(result.siteName).isNull()
        assertThat(result.imageUrl).isNull()
        assertThat(result.videoUrl).isNull()
        assertThat(result.videoType).isNull()
    }

    @Test
    fun shouldResolveNullValues_whenNodesAreJsonNull() {
        // given
        val embed = NetworkEmbed(
            kind = "link",
            title = "title",
            site = objectMapper.nullNode(),
            image = objectMapper.nullNode(),
            video = objectMapper.nullNode()
        )

        // when
        val result = embed.toEmbed()

        // then
        assertThat(result.siteName).isNull()
        assertThat(result.imageUrl).isNull()
        assertThat(result.videoUrl).isNull()
        assertThat(result.videoType).isNull()
    }

    @Test
    fun shouldResolveNullSiteName_whenNameIsMissing() {
        // given
        val embed = NetworkEmbed(
            site = objectMapper.readTree("""{}"""),
            image = objectMapper.readTree("""{}""")
        )

        // when
        val result = embed.toEmbed()

        // then
        assertThat(result.siteName).isNull()
        assertThat(result.imageUrl).isNull()
    }
}
