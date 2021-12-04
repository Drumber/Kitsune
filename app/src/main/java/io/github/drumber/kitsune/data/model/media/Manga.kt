package io.github.drumber.kitsune.data.model.media

import androidx.room.Embedded
import androidx.room.Ignore
import androidx.room.PrimaryKey
import com.fasterxml.jackson.annotation.JsonProperty
import com.github.jasminb.jsonapi.annotations.Id
import com.github.jasminb.jsonapi.annotations.Relationship
import com.github.jasminb.jsonapi.annotations.Type
import io.github.drumber.kitsune.data.model.category.Category
import io.github.drumber.kitsune.data.model.mediarelationship.MediaRelationship
import kotlinx.parcelize.Parcelize

@Parcelize
@Type("manga")
data class Manga @Ignore constructor(
    @PrimaryKey @Id
    override val id: String = "",
    override val slug: String?,
    override val description: String?,
    @Embedded(prefix = "titles_")
    override val titles: Titles?,
    override val canonicalTitle: String?,
    override val abbreviatedTitles: List<String>?,
    override val averageRating: String?,
    @Embedded(prefix = "rating_")
    override val ratingFrequencies: Rating?,
    override val userCount: Int?,
    override val favoritesCount: Int?,
    override val startDate: String?,
    override val endDate: String?,
    override val popularityRank: Int?,
    override val ratingRank: Int?,
    override val ageRating: AgeRating?,
    override val ageRatingGuide: String?,
    val subtype: MangaSubtype?,
    override val status: Status?,
    override val tba: String?,
    @Embedded(prefix = "poster_")
    override val posterImage: Image?,
    @Embedded(prefix = "cover_")
    override val coverImage: Image?,
    val chapterCount: Int?,
    val volumeCount: Int?,
    val serialization: String?,
    override val nsfw: Boolean?,
    override val nextRelease: String?,
    override val totalLength: Int?,
    @Ignore
    @Relationship("categories")
    override val categories: List<Category>? = null,
    @Ignore
    @Relationship("mediaRelationships")
    override val mediaRelationships: List<MediaRelationship>? = null
) : BaseMedia() {

    /**
     * Secondary constructor for Room
     */
    constructor(
        id: String,
        slug: String?,
        description: String?,
        titles: Titles?,
        canonicalTitle: String?,
        abbreviatedTitles: List<String>?,
        averageRating: String?,
        ratingFrequencies: Rating?,
        userCount: Int?,
        favoritesCount: Int?,
        startDate: String?,
        endDate: String?,
        popularityRank: Int?,
        ratingRank: Int?,
        ageRating: AgeRating?,
        ageRatingGuide: String?,
        subtype: MangaSubtype?,
        status: Status?,
        tba: String?,
        posterImage: Image?,
        coverImage: Image?,
        chapterCount: Int?,
        volumeCount: Int?,
        serialization: String?,
        nsfw: Boolean?,
        nextRelease: String?,
        totalLength: Int?,
    ) : this(
        id,
        slug,
        description,
        titles,
        canonicalTitle,
        abbreviatedTitles,
        averageRating,
        ratingFrequencies,
        userCount,
        favoritesCount,
        startDate,
        endDate,
        popularityRank,
        ratingRank,
        ageRating,
        ageRatingGuide,
        subtype,
        status,
        tba,
        posterImage,
        coverImage,
        chapterCount,
        volumeCount,
        serialization,
        nsfw,
        nextRelease,
        totalLength,
        null,
        null
    )

}

enum class MangaSubtype {
    @JsonProperty("doujin")
    Doujin,
    @JsonProperty("manga")
    Manga,
    @JsonProperty("manhua")
    Manhua,
    @JsonProperty("manhwa")
    Manhwa,
    @JsonProperty("novel")
    Novel,
    @JsonProperty("oel")
    Oel,
    @JsonProperty("oneshot")
    Oneshot
}
