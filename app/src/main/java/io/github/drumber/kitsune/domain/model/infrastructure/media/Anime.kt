package io.github.drumber.kitsune.domain.model.infrastructure.media

import com.github.jasminb.jsonapi.annotations.Id
import com.github.jasminb.jsonapi.annotations.Relationship
import com.github.jasminb.jsonapi.annotations.Type
import io.github.drumber.kitsune.domain.model.infrastructure.image.Image
import io.github.drumber.kitsune.domain.model.infrastructure.media.category.Category
import io.github.drumber.kitsune.domain.model.infrastructure.media.mediarelationship.MediaRelationship
import io.github.drumber.kitsune.domain.model.infrastructure.media.streamer.StreamingLink
import io.github.drumber.kitsune.domain.model.infrastructure.production.AnimeProduction
import kotlinx.parcelize.Parcelize

@Parcelize
@Type("anime")
data class Anime(
    @Id
    override val id: String = "",
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

    @Relationship("categories")
    override val categories: List<Category>?,
    @Relationship("animeProductions")
    val animeProduction: List<AnimeProduction>?,
    @Relationship("streamingLinks")
    val streamingLinks: List<StreamingLink>?,
    @Relationship("mediaRelationships")
    override val mediaRelationships: List<MediaRelationship>?
) : BaseMedia
