package io.github.drumber.kitsune.data.model.media

import android.os.Parcelable
import androidx.room.Embedded
import androidx.room.Ignore
import androidx.room.PrimaryKey
import com.fasterxml.jackson.annotation.JsonProperty
import com.github.jasminb.jsonapi.annotations.Id
import com.github.jasminb.jsonapi.annotations.Relationship
import com.github.jasminb.jsonapi.annotations.Type
import io.github.drumber.kitsune.data.model.category.Category
import io.github.drumber.kitsune.data.model.mediarelationship.MediaRelationship
import io.github.drumber.kitsune.data.model.production.AnimeProduction
import io.github.drumber.kitsune.data.model.streamer.StreamingLink
import kotlinx.parcelize.Parcelize

@Parcelize
@Type("anime")
data class Anime @Ignore constructor(
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
    val subtype: AnimeSubtype?,
    override val status: Status?,
    override val tba: String?,
    @Embedded(prefix = "poster_")
    override val posterImage: Image?,
    @Embedded(prefix = "cover_")
    override val coverImage: Image?,
    val episodeCount: Int?,
    val episodeLength: Int?,
    val youtubeVideoId: String?,
    override val nsfw: Boolean?,
    override val nextRelease: String?,
    override val totalLength: Int?,
    @Ignore
    @Relationship("categories")
    override val categories: List<Category>? = null,
    @Ignore
    @Relationship("animeProductions")
    val animeProduction: List<AnimeProduction>? = null,
    @Ignore
    @Relationship("streamingLinks")
    val streamingLinks: List<StreamingLink>? = null,
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
        subtype: AnimeSubtype?,
        status: Status?,
        tba: String?,
        posterImage: Image?,
        coverImage: Image?,
        episodeCount: Int?,
        episodeLength: Int?,
        youtubeVideoId: String?,
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
        episodeCount,
        episodeLength,
        youtubeVideoId,
        nsfw,
        nextRelease,
        totalLength,
        null,
        null,
        null,
        null
    )

}

@Parcelize
data class Rating(
    @JsonProperty("2") val r2: String?,
    @JsonProperty("3") val r3: String?,
    @JsonProperty("4") val r4: String?,
    @JsonProperty("5") val r5: String?,
    @JsonProperty("6") val r6: String?,
    @JsonProperty("7") val r7: String?,
    @JsonProperty("8") val r8: String?,
    @JsonProperty("9") val r9: String?,
    @JsonProperty("10") val r10: String?,
    @JsonProperty("11") val r11: String?,
    @JsonProperty("12") val r12: String?,
    @JsonProperty("13") val r13: String?,
    @JsonProperty("14") val r14: String?,
    @JsonProperty("15") val r15: String?,
    @JsonProperty("16") val r16: String?,
    @JsonProperty("17") val r17: String?,
    @JsonProperty("18") val r18: String?,
    @JsonProperty("19") val r19: String?,
    @JsonProperty("20") val r20: String?,
) : Parcelable

enum class AgeRating {
    G,
    PG,
    R,
    R18
}

enum class AnimeSubtype {
    ONA,
    OVA,
    TV,
    @JsonProperty("movie")
    Movie,
    @JsonProperty("music")
    Music,
    @JsonProperty("special")
    Special
}

enum class Status {
    @JsonProperty("current")
    Current,
    @JsonProperty("finished")
    Finished,
    @JsonProperty("tba")
    TBA,
    @JsonProperty("unreleased")
    Unreleased,
    @JsonProperty("upcoming")
    Upcoming
}
