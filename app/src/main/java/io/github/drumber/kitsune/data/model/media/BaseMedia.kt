package io.github.drumber.kitsune.data.model.media

import android.os.Parcelable
import io.github.drumber.kitsune.data.model.category.Category
import io.github.drumber.kitsune.data.model.mediarelationship.MediaRelationship
import io.github.drumber.kitsune.data.model.user.FavoriteItem

sealed class BaseMedia : Media, FavoriteItem, Parcelable

interface Media : Parcelable {
    val id: String
    val slug: String?
    val description: String?
    val titles: Titles?
    val canonicalTitle: String?
    val abbreviatedTitles: List<String>?
    val averageRating: String?
    val ratingFrequencies: Rating?
    val userCount: Int?
    val favoritesCount: Int?
    val startDate: String?
    val endDate: String?
    val popularityRank: Int?
    val ratingRank: Int?
    val ageRating: AgeRating?
    val ageRatingGuide: String?
    val status: Status?
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
