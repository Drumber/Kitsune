package io.github.drumber.kitsune.domain_old.model.infrastructure.media

import com.github.jasminb.jsonapi.annotations.Id
import com.github.jasminb.jsonapi.annotations.Relationship
import com.github.jasminb.jsonapi.annotations.Type
import io.github.drumber.kitsune.domain_old.model.common.media.AgeRating
import io.github.drumber.kitsune.domain_old.model.common.media.MangaSubtype
import io.github.drumber.kitsune.domain_old.model.common.media.RatingFrequencies
import io.github.drumber.kitsune.domain_old.model.common.media.ReleaseStatus
import io.github.drumber.kitsune.domain_old.model.common.media.Titles
import io.github.drumber.kitsune.domain_old.model.infrastructure.image.Image
import io.github.drumber.kitsune.domain_old.model.infrastructure.media.category.Category
import io.github.drumber.kitsune.domain_old.model.infrastructure.media.mediarelationship.MediaRelationship
import kotlinx.parcelize.Parcelize

@Parcelize
@Type("manga")
data class Manga(
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
    val chapterCount: Int?,
    val volumeCount: Int?,
    val subtype: MangaSubtype?,
    val serialization: String?,

    @Relationship("categories")
    override val categories: List<Category>?,
    @Relationship("mediaRelationships")
    override val mediaRelationships: List<MediaRelationship>?
) : BaseMedia
