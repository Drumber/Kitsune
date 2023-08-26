package io.github.drumber.kitsune.domain.model.database

import android.os.Parcelable
import io.github.drumber.kitsune.domain.model.infrastructure.image.Image
import io.github.drumber.kitsune.domain.model.infrastructure.media.AgeRating
import io.github.drumber.kitsune.domain.model.infrastructure.media.RatingFrequencies
import io.github.drumber.kitsune.domain.model.infrastructure.media.ReleaseStatus
import kotlinx.parcelize.Parcelize

@Parcelize
data class LocalMediaEntry(
    val id: String,
    val isAnime: Boolean,

    val description: String?,
    val titles: Map<String, String>?,
    val canonicalTitle: String?,
    val abbreviatedTitles: List<String>?,

    val averageRating: String?,
    val ratingFrequencies: RatingFrequencies?,
    val userCount: Int?,
    val favoritesCount: Int?,
    val popularityRank: Int?,
    val ratingRank: Int?,

    val startDate: String?,
    val endDate: String?,
    val nextRelease: String?,
    val tba: String?,
    val status: ReleaseStatus?,

    val ageRating: AgeRating?,
    val ageRatingGuide: String?,
    val nsfw: Boolean?,

    val posterImage: Image?,
    val coverImage: Image?,

    val totalLength: Int?
) : Parcelable
