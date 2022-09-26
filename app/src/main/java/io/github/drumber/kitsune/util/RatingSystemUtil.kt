package io.github.drumber.kitsune.util

import io.github.drumber.kitsune.data.model.user.RatingSystem
import io.github.drumber.kitsune.data.repository.UserRepository
import org.koin.core.component.KoinComponent
import org.koin.core.component.get
import kotlin.math.floor
import kotlin.math.roundToInt

object RatingSystemUtil : KoinComponent {

    private val DEFAULT = RatingSystem.Regular

    fun getRatingSystem(): RatingSystem {
        val userRepository: UserRepository = get()
        return userRepository.user?.ratingSystem ?: DEFAULT
    }

    fun formatRating(ratingTwenty: Int, ratingSystem: RatingSystem = getRatingSystem()): String {
        return ratingSystem.convertFrom(ratingTwenty).toString()
    }

    fun Int.formatRatingTwenty() = formatRating(this)

    fun Int.fromRatingTwentyTo(ratingSystem: RatingSystem = getRatingSystem()) =
        ratingSystem.convertFrom(this)

    fun Float.toRatingTwentyFrom(ratingSystem: RatingSystem = getRatingSystem()) =
        ratingSystem.convertToRatingTwenty(this)

    fun RatingSystem.convertFrom(ratingTwenty: Int): Float {
        return when (this) {
            RatingSystem.Simple -> when (ratingTwenty) {
                in 1..7 -> 1f
                in 8..13 -> 2f
                in 14..19 -> 3f
                else -> 4f
            }
            RatingSystem.Regular -> (ratingTwenty / 2.0f).roundToInt() / 2.0f
            RatingSystem.Advanced -> ratingTwenty / 2.0f
        }
    }

    fun RatingSystem.convertToRatingTwenty(rating: Float): Int {
        return when (this) {
            RatingSystem.Simple -> floor(rating * 5).toInt()
            RatingSystem.Regular -> floor(rating * 4).toInt()
            RatingSystem.Advanced -> floor(rating * 2).toInt()
        }
    }

    fun RatingSystem.stepSize(): Float {
        return when (this) {
            RatingSystem.Simple -> 1f
            RatingSystem.Regular -> 0.5f
            RatingSystem.Advanced -> 0.5f
        }
    }

}