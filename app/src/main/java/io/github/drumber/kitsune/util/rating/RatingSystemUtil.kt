package io.github.drumber.kitsune.util.rating

import io.github.drumber.kitsune.data.repository.UserRepository
import io.github.drumber.kitsune.data.source.local.user.model.LocalRatingSystemPreference
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import kotlin.math.floor

object RatingSystemUtil : KoinComponent {

    private val DEFAULT = LocalRatingSystemPreference.Regular

    private val userRepository: UserRepository by inject()

    fun getRatingSystem(): LocalRatingSystemPreference {
        return userRepository.localUser.value?.ratingSystem ?: DEFAULT
    }

    fun formatRating(ratingTwenty: Int, ratingSystem: LocalRatingSystemPreference = getRatingSystem()): String {
        return ratingSystem.convertFrom(ratingTwenty).toString()
    }

    fun Int.formatRatingTwenty() = formatRating(this)

    fun Int.fromRatingTwentyTo(ratingSystem: LocalRatingSystemPreference = getRatingSystem()) =
        ratingSystem.convertFrom(this)

    fun Float.toRatingTwentyFrom(ratingSystem: LocalRatingSystemPreference = getRatingSystem()) =
        ratingSystem.convertToRatingTwenty(this)

    fun LocalRatingSystemPreference.convertFrom(ratingTwenty: Int): Float {
        return when (this) {
            LocalRatingSystemPreference.Simple -> when (ratingTwenty) {
                in 1..7 -> 1f
                in 8..13 -> 2f
                in 14..19 -> 3f
                else -> 4f
            }
            LocalRatingSystemPreference.Regular -> floor(ratingTwenty / 2.0f) / 2.0f
            LocalRatingSystemPreference.Advanced -> ratingTwenty / 2.0f
        }
    }

    fun LocalRatingSystemPreference.convertToRatingTwenty(rating: Float): Int {
        return when (this) {
            LocalRatingSystemPreference.Simple -> when (rating) {
                1f -> 2
                2f -> 8
                3f -> 14
                else -> 20
            }
            LocalRatingSystemPreference.Regular -> floor(rating * 4).toInt()
            LocalRatingSystemPreference.Advanced -> floor(rating * 2).toInt()
        }
    }

    fun LocalRatingSystemPreference.stepSize(): Float {
        return when (this) {
            LocalRatingSystemPreference.Simple -> 1f
            LocalRatingSystemPreference.Regular -> 0.5f
            LocalRatingSystemPreference.Advanced -> 0.5f
        }
    }

}