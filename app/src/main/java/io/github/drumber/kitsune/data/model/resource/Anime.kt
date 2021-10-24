package io.github.drumber.kitsune.data.model.resource

import android.os.Parcelable
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.fasterxml.jackson.annotation.JsonProperty
import com.github.jasminb.jsonapi.annotations.Id
import com.github.jasminb.jsonapi.annotations.Type
import kotlinx.parcelize.Parcelize

@Parcelize
@Entity(tableName = "anime_table")
@Type("anime")
data class Anime(
    @PrimaryKey @Id
    val id: String = "",
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
    val subtype: AnimeSubtype?,
    val status: Status?,
    val tba: String?,
    @Embedded(prefix = "poster_") val posterImage: Image?,
    @Embedded(prefix = "cover_") val coverImage: Image?,
    val episodeCount: Int?,
    val episodeLength: Int?,
    val youtubeVideoId: String?,
    val nsfw: Boolean?,
    val nextRelease: String?,
    val totalLength: Int?
) : Resource(), Parcelable

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
    movie,
    music,
    special
}

enum class Status {
    current,
    finished,
    tba,
    unreleased,
    upcoming
}
