package io.github.drumber.kitsune.data.model.resource

import android.os.Parcelable
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.github.jasminb.jsonapi.annotations.Id
import com.github.jasminb.jsonapi.annotations.Type
import kotlinx.parcelize.Parcelize

@Parcelize
@Entity(tableName = "manga_table")
@Type("manga")
data class Manga(
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
    val totalLength: Int?
) : Resource(), Parcelable

enum class MangaSubtype {
    doujin,
    manga,
    manhua,
    manhwa,
    novel,
    oel,
    oneshot
}
