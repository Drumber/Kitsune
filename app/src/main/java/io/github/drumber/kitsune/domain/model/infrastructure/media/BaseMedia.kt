package io.github.drumber.kitsune.domain.model.infrastructure.media

import android.os.Parcelable
import io.github.drumber.kitsune.domain.model.common.media.AgeRating
import io.github.drumber.kitsune.domain.model.common.media.RatingFrequencies
import io.github.drumber.kitsune.domain.model.common.media.ReleaseStatus
import io.github.drumber.kitsune.domain.model.common.media.Titles
import io.github.drumber.kitsune.domain.model.infrastructure.image.Image
import io.github.drumber.kitsune.domain.model.infrastructure.media.category.Category
import io.github.drumber.kitsune.domain.model.infrastructure.media.mediarelationship.MediaRelationship
import io.github.drumber.kitsune.domain.model.infrastructure.user.FavoriteItem

sealed interface BaseMedia : FavoriteItem, Parcelable {
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
    val popularityRank: Int?
    val ratingRank: Int?

    val startDate: String?
    val endDate: String?
    val nextRelease: String?
    val tba: String?
    val status: ReleaseStatus?

    val ageRating: AgeRating?
    val ageRatingGuide: String?
    val nsfw: Boolean?

    val posterImage: Image?
    val coverImage: Image?

    val totalLength: Int?

    // Relationships
    val categories: List<Category>?
    val mediaRelationships: List<MediaRelationship>?
}