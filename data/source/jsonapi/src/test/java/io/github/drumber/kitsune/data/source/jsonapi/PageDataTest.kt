package io.github.drumber.kitsune.data.source.jsonapi

import com.github.jasminb.jsonapi.JSONAPIDocument
import com.github.jasminb.jsonapi.JSONAPISpecConstants
import com.github.jasminb.jsonapi.Link
import com.github.jasminb.jsonapi.Links
import io.github.drumber.kitsune.data.source.jsonapi.testutils.networkAnime
import net.datafaker.Faker
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class PageDataTest {

    private val faker = Faker()

    @Test
    fun shouldMap_JSONAPIDocument_to_PageData() {
        // given
        val data = List(5) { networkAnime(faker) }

        val links = mutableMapOf(
            JSONAPISpecConstants.FIRST to Link("https://example.com/api/edge/anime?page%5Blimit%5D=5&page%5Boffset%5D=0"),
            JSONAPISpecConstants.LAST to Link("https://example.com/api/edge/anime?page%5Blimit%5D=5&page%5Boffset%5D=20541"),
            JSONAPISpecConstants.NEXT to Link("https://example.com/api/edge/anime?page%5Blimit%5D=5&page%5Boffset%5D=5")
        )

        val jsonApiDocument = JSONAPIDocument(data, Links(links), emptyMap())

        // when
        val pageData = jsonApiDocument.toPageData()

        // then
        assertThat(pageData.data).isEqualTo(data)
        assertThat(pageData.first).isEqualTo(0)
        assertThat(pageData.last).isEqualTo(20541)
        assertThat(pageData.next).isEqualTo(5)
        assertThat(pageData.prev).isNull()
    }
}