package io.github.drumber.kitsune.util.rating

import io.github.drumber.kitsune.data.presentation.model.media.RatingFrequencies
import io.github.drumber.kitsune.data.source.local.user.model.LocalRatingSystemPreference
import io.github.drumber.kitsune.util.rating.RatingFrequenciesUtil.calculateAverageRating
import io.github.drumber.kitsune.util.rating.RatingFrequenciesUtil.transformToRatingSystem
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class RatingFrequenciesUtilTest {

    @Test
    fun shouldTransformToRatingSystem() {
        // given
        val ratings = ratingFrequencies(2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20)
        val parameters = mapOf(
            LocalRatingSystemPreference.Simple to listOf(27, 63, 99, 20),
            LocalRatingSystemPreference.Regular to listOf(5, 9, 13, 17, 21, 25, 29, 33, 37, 20),
            LocalRatingSystemPreference.Advanced to listOf(2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20)
        )

        parameters.forEach { (ratingSystem, expectedOutput) ->
            // when
            val mappedRatings = ratings.transformToRatingSystem(ratingSystem)

            // then
            assertThat(mappedRatings).`as`("RatingSystem '${ratingSystem.name}'").isEqualTo(expectedOutput)
        }
    }

    @Test
    fun shouldCalculateAverageRating() {
        // given
        val testScenarios = listOf(
            TestScenario(
                input = ratingFrequencies(10, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 10),
                expectedOutput = mapOf(
                    LocalRatingSystemPreference.Simple to 2.5,
                    LocalRatingSystemPreference.Regular to 2.75,
                    LocalRatingSystemPreference.Advanced to 5.5
                )
            ),
            TestScenario(
                input = ratingFrequencies(1, 0, 0, 0, 1, 1, 1, 0, 1, 0, 3, 1, 4, 1, 0, 0, 0, 1, 1),
                expectedOutput = mapOf(
                    LocalRatingSystemPreference.Simple to 2.3125,
                    LocalRatingSystemPreference.Regular to 2.9375,
                    LocalRatingSystemPreference.Advanced to 6.0
                )
            )
        )

        testScenarios.forEach { (ratings, testCases) ->
            testCases.forEach { (ratingSystem, expectedOutput) ->
                // when
                val average = ratings.calculateAverageRating(ratingSystem)

                // then
                assertThat(average).`as`("RatingSystem '${ratingSystem.name}'").isEqualTo(expectedOutput)
            }
        }
    }

    private fun ratingFrequencies(vararg ratingCounts: Int): RatingFrequencies {
        require(ratingCounts.size == 19)
        val r = ratingCounts.map(Int::toString)
        return RatingFrequencies(
            r[0],
            r[1],
            r[2],
            r[3],
            r[4],
            r[5],
            r[6],
            r[7],
            r[8],
            r[9],
            r[10],
            r[11],
            r[12],
            r[13],
            r[14],
            r[15],
            r[16],
            r[17],
            r[18]
        )
    }

    data class TestScenario<out INPUT, out OUTPUT>(
        val input: INPUT,
        val expectedOutput: OUTPUT
    )
}