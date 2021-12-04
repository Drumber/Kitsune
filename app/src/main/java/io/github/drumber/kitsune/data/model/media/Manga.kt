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
    val createdAt: String?,
    val updatedAt: String?,
    val slug: String?,
    val description: String?,
    @Embedded(prefix = "titles_") val titles: Titles?,
    val canonicalTitle: String?,
    val abbreviatedTitles: List<String>?,
    val averageRating: String?,
    @Embedded(prefix = "rating_") val ratingFrequencies: Rating?,
    val userCount: Int?,
    val favoritesCount: Int?,
    val startDate: String?,
    val endDate: String?,
    val popularityRank: Int?,
    val ratingRank: Int?,
    val ageRating: AgeRating?,
    val ageRatingGuide: String?,
    val subtype: MangaSubtype?,
    val status: Status?,
    val tba: String?,
    @Embedded(prefix = "poster_") val posterImage: Image?,
    @Embedded(prefix = "cover_") val coverImage: Image?,
    val chapterCount: Int?,
    val volumeCount: Int?,
    val serialization: String?,
    val nsfw: Boolean?,
    val nextRelease: String?,
    val totalLength: Int?,
    @Ignore
    @Relationship("categories")
    val categories: List<Category>? = null,
    @Ignore
    @Relationship("mediaRelationships")
    val mediaRelationships: List<MediaRelationship>? = null
) : BaseMedia() {

    /**
     * Secondary constructor for Room
     */
    constructor(
        id: String,
        createdAt: String?,
        updatedAt: String?,
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
        createdAt,
        updatedAt,
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
