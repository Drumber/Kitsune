package io.github.drumber.kitsune.domain.mapper

import io.github.drumber.kitsune.domain.model.infrastructure.algolia.search.AlgoliaDimension
import io.github.drumber.kitsune.domain.model.infrastructure.algolia.search.AlgoliaDimensions
import io.github.drumber.kitsune.domain.model.infrastructure.algolia.search.AlgoliaImage
import io.github.drumber.kitsune.domain.model.infrastructure.algolia.search.AlgoliaImageMeta
import io.github.drumber.kitsune.domain.model.infrastructure.algolia.search.MediaSearchKind
import io.github.drumber.kitsune.domain.model.infrastructure.algolia.search.MediaSearchResult
import io.github.drumber.kitsune.domain.model.infrastructure.image.Dimension
import io.github.drumber.kitsune.domain.model.infrastructure.image.Dimensions
import io.github.drumber.kitsune.domain.model.infrastructure.image.Image
import io.github.drumber.kitsune.domain.model.infrastructure.image.ImageMeta
import io.github.drumber.kitsune.testutils.anime
import net.datafaker.Faker
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class MediaSearchResultMapperTest {

    private val faker = Faker()

    @Test
    fun shouldMapMediaSearchResultToBaseMedia() {
        // given
        val givenAnime = anime(faker)
        val mediaSearchResult = MediaSearchResult(
            givenAnime.id.toLong(),
            MediaSearchKind.Anime,
            givenAnime.subtype?.name,
            givenAnime.slug,
            givenAnime.titles,
            givenAnime.canonicalTitle,
            givenAnime.posterImage?.toAlgoliaImage()
        )

        // when
        val anime = mediaSearchResult.toMedia()

        // then
        assertThat(anime)
            .usingRecursiveComparison()
            .ignoringFields(
                "abbreviatedTitles",
                "ageRating",
                "ageRatingGuide",
                "averageRating",
                "coverImage",
                "description",
                "endDate",
                "episodeCount",
                "episodeLength",
                "favoritesCount",
                "nextRelease",
                "nsfw",
                "popularityRank",
                "ratingFrequencies",
                "ratingRank",
                "startDate",
                "status",
                "tba",
                "totalLength",
                "userCount",
                "youtubeVideoId"
            )
            .isEqualTo(givenAnime)
    }

    private fun Image.toAlgoliaImage() = AlgoliaImage(
        tiny, small, medium, large, original, meta?.toAlgoliaImageMeta()
    )

    private fun ImageMeta.toAlgoliaImageMeta() = AlgoliaImageMeta(dimensions?.toAlgoliaDimensions())
    private fun Dimensions.toAlgoliaDimensions() = AlgoliaDimensions(
        tiny?.toAlgoliaDimension(),
        small?.toAlgoliaDimension(),
        medium?.toAlgoliaDimension(),
        large?.toAlgoliaDimension()
    )

    private fun Dimension.toAlgoliaDimension() = AlgoliaDimension(width, height)

}