package io.github.drumber.kitsune.data.model.resource.manga

import android.os.Parcelable
import com.github.jasminb.jsonapi.annotations.Id
import com.github.jasminb.jsonapi.annotations.Type
import io.github.drumber.kitsune.data.model.resource.Image
import io.github.drumber.kitsune.data.model.resource.Titles
import io.github.drumber.kitsune.data.model.resource.anime.AgeRating
import io.github.drumber.kitsune.data.model.resource.anime.Rating
import io.github.drumber.kitsune.data.model.resource.anime.Status
import kotlinx.parcelize.Parcelize

@Parcelize
@Type("manga")
data class Manga(
    @Id val id: String?,
    val createdAt: String?,
    val updatedAt: String?,
    val slug: String?,
    val description: String?,
    val titles: Titles?,
    val canonicalTitle: String?,
    val abbreviatedTitles: List<String>?,
    val averageRating: String?,
    val ratingFrequencies: Rating?,
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
    val posterImage: Image?,
    val coverImage: Image?,
    val chapterCount: Int?,
    val volumeCount: Int?,
    val serialization: String?,
    val nsfw: Boolean?,
    val nextRelease: String?,
    val totalLength: Int?
) : Parcelable

enum class MangaSubtype {
    doujin,
    manga,
    manhua,
    manhwa,
    novel,
    oel,
    oneshot
}
