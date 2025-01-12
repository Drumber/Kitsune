package io.github.drumber.kitsune.util.rating

import io.github.drumber.kitsune.data.model.media.RatingFrequencies
import io.github.drumber.kitsune.data.source.local.user.model.LocalRatingSystemPreference
import io.github.drumber.kitsune.util.rating.RatingSystemUtil.convertFrom

object RatingFrequenciesUtil {

    fun RatingFrequencies.transformToRatingSystem(ratingSystem: LocalRatingSystemPreference): List<Int> {
        val ratingCounts = this.toList().map { it?.toIntOrNull() ?: 0 }

        // contains the rating counts categorized by the rating type (1-4 for simple, 0.5-5 for regular, 1-10 for advanced)
        val ratingsMap = mutableMapOf<String, Int>()

        ratingCounts.forEachIndexed { index, value ->
            val ratingValue = ratingSystem.convertFrom(index + 2).toString()
            val prevValue = ratingsMap[ratingValue]
            if (prevValue != null) {
                ratingsMap[ratingValue] = prevValue + value
            } else {
                ratingsMap[ratingValue] = value
            }
        }

        return ratingsMap.values.toList()
    }

    fun RatingFrequencies.calculateAverageRating(ratingSystem: LocalRatingSystemPreference): Double {
        val ratingCounts = this.toList().map { it?.toIntOrNull() ?: 0 }
        if (ratingCounts.isEmpty()) return 0.0

        var sum = 0.0
        var totalRatings = 0
        for (i in ratingCounts.indices) {
            val count = ratingCounts[i]

            sum += count * ratingSystem.convertFrom(i + 2)
            totalRatings += count
        }

        if (totalRatings == 0) return 0.0
        return sum / totalRatings
    }

    fun RatingFrequencies.toList() = listOf(
        r2,
        r3,
        r4,
        r5,
        r6,
        r7,
        r8,
        r9,
        r10,
        r11,
        r12,
        r13,
        r14,
        r15,
        r16,
        r17,
        r18,
        r19,
        r20
    )

}