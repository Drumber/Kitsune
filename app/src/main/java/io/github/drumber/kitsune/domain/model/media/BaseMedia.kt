package io.github.drumber.kitsune.domain.model.media

import android.os.Parcelable
import io.github.drumber.kitsune.domain.model.infrastructure.image.Image
import io.github.drumber.kitsune.domain.model.infrastructure.media.AgeRating
import io.github.drumber.kitsune.domain.model.infrastructure.media.RatingFrequencies
import io.github.drumber.kitsune.domain.model.infrastructure.media.ReleaseStatus
import io.github.drumber.kitsune.domain.model.infrastructure.media.Titles
import io.github.drumber.kitsune.domain.model.infrastructure.media.category.Category
import io.github.drumber.kitsune.domain.model.infrastructure.media.mediarelationship.MediaRelationship
import io.github.drumber.kitsune.domain.model.infrastructure.user.FavoriteItem

// TODO: delete this
sealed class BaseMedia : Media, FavoriteItem, Parcelable

interface Media : Parcelable {
    val id: String
    val slug: String?
    val description: String?
    val titles: Titles?
    val canonicalTitle: String?
    val abbreviatedTitles: List<String>?
    val averageRating: String?
    val ratingFrequencies: RatingFrequencies?
    val userCount: Int?
    val favoritesCount: Int?
    val startDate: String?
    val endDate: String?
    val popularityRank: Int?
    val ratingRank: Int?
    val ageRating: AgeRating?
    val ageRatingGuide: String?
    val status: ReleaseStatus?
    val tba: String?
    val posterImage: Image?
    val coverImage: Image?
    val nsfw: Boolean?
    val nextRelease: String?
    val totalLength: Int?

    // Relationships
    val categories: List<Category>?
    val mediaRelationships: List<MediaRelationship>?
}
