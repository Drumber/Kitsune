package io.github.drumber.kitsune.data.source.network.media

import io.github.drumber.kitsune.data.common.Filter
import io.github.drumber.kitsune.data.source.network.media.api.AnimeApi
import io.github.drumber.kitsune.data.source.network.media.model.NetworkAnime
import io.github.drumber.kitsune.di.createObjectMapper
import io.github.drumber.kitsune.di.createService
import kotlinx.coroutines.test.runTest
import mockwebserver3.MockResponse
import mockwebserver3.MockWebServer
import okhttp3.OkHttpClient
import org.assertj.core.api.Assertions.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

/**
 * End-to-end deserialization tests for [AnimeNetworkDataSource]. A [MockWebServer] serves raw
 * JSON:API payloads which are deserialized by the real Retrofit + jsonapi-converter stack, so this
 * catches model annotation / type registration regressions that pure mapper tests cannot.
 */
@RunWith(RobolectricTestRunner::class)
class AnimeNetworkDataSourceTest {

    private lateinit var server: MockWebServer
    private lateinit var dataSource: AnimeNetworkDataSource

    @Before
    fun setup() {
        server = MockWebServer()
        server.start()
        val service = createService<AnimeApi>(
            OkHttpClient(),
            createObjectMapper(),
            NetworkAnime::class.java,
            baseUrl = server.url("/").toString()
        )
        dataSource = AnimeNetworkDataSource(service)
    }

    @After
    fun tearDown() {
        server.close()
    }

    private fun enqueue(body: String) {
        server.enqueue(MockResponse.Builder().code(200).body(body).build())
    }

    @Test
    fun shouldDeserializeAnimeList_withPageLinks() = runTest {
        // given a JSON:API list document with offset based page links
        enqueue(
            """
            {
              "data": [
                {
                  "id": "1",
                  "type": "anime",
                  "attributes": {
                    "slug": "cowboy-bebop",
                    "canonicalTitle": "Cowboy Bebop",
                    "averageRating": "82.41",
                    "episodeCount": 26
                  }
                },
                {
                  "id": "2",
                  "type": "anime",
                  "attributes": {
                    "slug": "trigun",
                    "canonicalTitle": "Trigun",
                    "episodeCount": 26
                  }
                }
              ],
              "links": {
                "first": "https://kitsu.io/api/edge/anime?page%5Boffset%5D=0",
                "next": "https://kitsu.io/api/edge/anime?page%5Boffset%5D=10"
              }
            }
            """.trimIndent()
        )

        // when
        val pageData = dataSource.getAllAnime(Filter())

        // then the resources are deserialized
        assertThat(pageData.data).hasSize(2)
        val first = pageData.data!!.first()
        assertThat(first.id).isEqualTo("1")
        assertThat(first.slug).isEqualTo("cowboy-bebop")
        assertThat(first.canonicalTitle).isEqualTo("Cowboy Bebop")
        assertThat(first.averageRating).isEqualTo("82.41")
        assertThat(first.episodeCount).isEqualTo(26)
        // and the page links are parsed into offsets
        assertThat(pageData.first).isEqualTo(0)
        assertThat(pageData.next).isEqualTo(10)
        assertThat(pageData.prev).isNull()
    }

    @Test
    fun shouldDeserializeSingleAnime() = runTest {
        // given a JSON:API single resource document
        enqueue(
            """
            {
              "data": {
                "id": "42",
                "type": "anime",
                "attributes": {
                  "slug": "naruto",
                  "canonicalTitle": "Naruto",
                  "episodeCount": 220
                }
              }
            }
            """.trimIndent()
        )

        // when
        val anime = dataSource.getAnime("42", Filter())

        // then
        assertThat(anime).isNotNull
        assertThat(anime!!.id).isEqualTo("42")
        assertThat(anime.canonicalTitle).isEqualTo("Naruto")
        assertThat(anime.episodeCount).isEqualTo(220)
    }
}
