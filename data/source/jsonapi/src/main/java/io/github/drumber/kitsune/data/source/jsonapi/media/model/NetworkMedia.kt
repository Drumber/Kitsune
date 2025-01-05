package io.github.drumber.kitsune.data.source.jsonapi.media.model

import io.github.drumber.kitsune.data.common.model.Image
import io.github.drumber.kitsune.data.common.model.Titles
import io.github.drumber.kitsune.data.common.model.media.AgeRating
import io.github.drumber.kitsune.data.source.jsonapi.media.model.category.NetworkCategory
import io.github.drumber.kitsune.data.source.jsonapi.media.model.relationship.NetworkMediaRelationship
import io.github.drumber.kitsune.data.source.jsonapi.user.model.NetworkFavoriteItem

sealed interface NetworkMedia : NetworkFavoriteItem {
    val id: String
    val slug: String?

    val description: String?
    val titles: Titles?
    val canonicalTitle: String?
    val abbreviatedTitles: List<String>?

    val averageRating: String?
    val ratingFrequencies: NetworkRatingFrequencies?
    val userCount: Int?
    val favoritesCount: Int?
    val popularityRank: Int?
    val ratingRank: Int?

    val startDate: String?
    val endDate: String?
    val nextRelease: String?
    val tba: String?
    val status: NetworkReleaseStatus?

    val ageRating: AgeRating?
    val ageRatingGuide: String?
    val nsfw: Boolean?

    val posterImage: Image?
    val coverImage: Image?

    val totalLength: Int?

    // Relationships
    val categories: List<NetworkCategory>?
    val mediaRelationships: List<NetworkMediaRelationship>?
}