package io.github.drumber.kitsune.data.source.local.library.model

import androidx.room.Embedded
import androidx.room.PrimaryKey
import io.github.drumber.kitsune.data.common.Titles
import io.github.drumber.kitsune.data.common.media.AgeRating
import io.github.drumber.kitsune.data.common.media.AnimeSubtype
import io.github.drumber.kitsune.data.common.media.MangaSubtype
import io.github.drumber.kitsune.data.common.media.RatingFrequencies
import io.github.drumber.kitsune.data.common.media.ReleaseStatus

data class LocalLibraryMedia(
    @PrimaryKey
    val id: String,
    val type: MediaType,

    val description: String?,
    val titles: Titles?,
    val canonicalTitle: String?,
    val abbreviatedTitles: List<String>?,

    val averageRating: String?,
    @Embedded(prefix = "rating_")
    val ratingFrequencies: RatingFrequencies?,
    val popularityRank: Int?,
    val ratingRank: Int?,

    val startDate: String?,
    val endDate: String?,
    val nextRelease: String?,
    val tba: String?,
    val status: ReleaseStatus?,

    val ageRating: AgeRating?,
    val ageRatingGuide: String?,
    val nsfw: Boolean?,

    @Embedded(prefix = "poster_")
    val posterImage: LocalImage?,
    @Embedded(prefix = "cover_")
    val coverImage: LocalImage?,

    // Anime specific attributes
    val animeSubtype: AnimeSubtype?,
    val totalLength: Int?,
    val episodeCount: Int?,
    val episodeLength: Int?,

    // Manga specific attributes
    val mangaSubtype: MangaSubtype?,
    val chapterCount: Int?,
    val volumeCount: Int?,
    val serialization: String?
) {
    enum class MediaType {
        Anime,
        Manga
    }
}