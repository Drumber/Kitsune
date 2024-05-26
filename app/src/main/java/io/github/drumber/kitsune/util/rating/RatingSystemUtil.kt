package io.github.drumber.kitsune.util.rating

import io.github.drumber.kitsune.domain.model.infrastructure.user.RatingSystemPreference
import io.github.drumber.kitsune.domain.repository.UserRepository
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import kotlin.math.floor

object RatingSystemUtil : KoinComponent {

    private val DEFAULT = RatingSystemPreference.Regular

    private val userRepository: UserRepository by inject()

    fun getRatingSystem(): RatingSystemPreference {
        return userRepository.user?.ratingSystem ?: DEFAULT
    }

    fun formatRating(ratingTwenty: Int, ratingSystem: RatingSystemPreference = getRatingSystem()): String {
        return ratingSystem.convertFrom(ratingTwenty).toString()
    }

    fun Int.formatRatingTwenty() = formatRating(this)

    fun Int.fromRatingTwentyTo(ratingSystem: RatingSystemPreference = getRatingSystem()) =
        ratingSystem.convertFrom(this)

    fun Float.toRatingTwentyFrom(ratingSystem: RatingSystemPreference = getRatingSystem()) =
        ratingSystem.convertToRatingTwenty(this)

    fun RatingSystemPreference.convertFrom(ratingTwenty: Int): Float {
        return when (this) {
            RatingSystemPreference.Simple -> when (ratingTwenty) {
                in 1..7 -> 1f
                in 8..13 -> 2f
                in 14..19 -> 3f
                else -> 4f
            }
            RatingSystemPreference.Regular -> floor(ratingTwenty / 2.0f) / 2.0f
            RatingSystemPreference.Advanced -> ratingTwenty / 2.0f
        }
    }

    fun RatingSystemPreference.convertToRatingTwenty(rating: Float): Int {
        return when (this) {
            RatingSystemPreference.Simple -> when (rating) {
                1f -> 2
                2f -> 8
                3f -> 14
                else -> 20
            }
            RatingSystemPreference.Regular -> floor(rating * 4).toInt()
            RatingSystemPreference.Advanced -> floor(rating * 2).toInt()
        }
    }

    fun RatingSystemPreference.stepSize(): Float {
        return when (this) {
            RatingSystemPreference.Simple -> 1f
            RatingSystemPreference.Regular -> 0.5f
            RatingSystemPreference.Advanced -> 0.5f
        }
    }

}