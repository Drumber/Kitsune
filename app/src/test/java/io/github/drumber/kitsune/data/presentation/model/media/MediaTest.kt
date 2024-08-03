package io.github.drumber.kitsune.data.presentation.model.media

import io.github.drumber.kitsune.R
import io.github.drumber.kitsune.testutils.anime
import net.datafaker.Faker
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class MediaTest {

    private val faker = Faker()

    @Test
    fun shouldGetCorrectSeasonString() {
        // given
        val anime = anime(faker)

        // when & then
        mapOf(
            "2023-12-01" to R.string.season_winter,
            "2024-02-29" to R.string.season_winter,
            "2024-03-01" to R.string.season_spring,
            "2024-05-31" to R.string.season_spring,
            "2024-06-01" to R.string.season_summer,
            "2024-08-31" to R.string.season_summer,
            "2024-09-01" to R.string.season_fall,
            "2024-11-30" to R.string.season_fall,
        ).forEach { (startDate, seasonStringRes) ->
            val stringRes = anime.copy(startDate = startDate).seasonStringRes
            assertThat(stringRes).`as`("Date $startDate").isEqualTo(seasonStringRes)
        }
    }

    @Test
    fun shouldGetCorrectSeasonYear() {
        // given
        val anime = anime(faker)

        // when & then
        mapOf(
            "2023-12-01" to "2024",
            "2024-02-29" to "2024",
            "2024-11-30" to "2024",
        ).forEach { (startDate, seasonYear) ->
            val year = anime.copy(startDate = startDate).seasonYear
            assertThat(year).`as`("Date $startDate").isEqualTo(seasonYear)
        }
    }
}