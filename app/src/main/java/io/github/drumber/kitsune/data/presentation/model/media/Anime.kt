package io.github.drumber.kitsune.data.presentation.model.media

import io.github.drumber.kitsune.data.model.Image
import io.github.drumber.kitsune.data.model.Titles
import io.github.drumber.kitsune.data.model.media.AgeRating
import io.github.drumber.kitsune.data.model.media.AnimeSubtype
import io.github.drumber.kitsune.data.model.media.MediaType
import io.github.drumber.kitsune.data.model.media.RatingFrequencies
import io.github.drumber.kitsune.data.model.media.ReleaseStatus
import io.github.drumber.kitsune.data.presentation.model.media.category.Category
import io.github.drumber.kitsune.data.presentation.model.media.production.AnimeProduction
import io.github.drumber.kitsune.data.presentation.model.media.relationship.MediaRelationship
import io.github.drumber.kitsune.data.presentation.model.media.streamer.StreamingLink

data class Anime(
    override val id: String,
    override val slug: String?,

    override val description: String?,
    override val titles: Titles?,
    override val canonicalTitle: String?,
    override val abbreviatedTitles: List<String>?,

    override val averageRating: String?,
    override val ratingFrequencies: RatingFrequencies?,
    override val userCount: Int?,
    override val favoritesCount: Int?,
    override val popularityRank: Int?,
    override val ratingRank: Int?,

    override val startDate: String?,
    override val endDate: String?,
    override val nextRelease: String?,
    override val tba: String?,
    override val status: ReleaseStatus?,

    override val ageRating: AgeRating?,
    override val ageRatingGuide: String?,
    override val nsfw: Boolean?,

    override val posterImage: Image?,
    override val coverImage: Image?,

    override val totalLength: Int?,
    val episodeCount: Int?,
    val episodeLength: Int?,
    val youtubeVideoId: String?,
    val subtype: AnimeSubtype?,

    override val categories: List<Category>?,
    val animeProduction: List<AnimeProduction>?,
    val streamingLinks: List<StreamingLink>?,
    override val mediaRelationships: List<MediaRelationship>?
) : Media() {

    override val mediaType: MediaType
        get() = MediaType.Anime

}
