package io.github.drumber.kitsune.domain.model.database

import android.os.Parcelable
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import io.github.drumber.kitsune.domain.model.infrastructure.media.AgeRating
import io.github.drumber.kitsune.domain.model.infrastructure.media.AnimeSubtype
import io.github.drumber.kitsune.domain.model.infrastructure.media.RatingFrequencies
import io.github.drumber.kitsune.domain.model.infrastructure.media.ReleaseStatus
import io.github.drumber.kitsune.domain.model.infrastructure.media.Titles
import kotlinx.parcelize.Parcelize

@Parcelize
@Entity("anime")
data class LocalAnime(
    @PrimaryKey
    val id: String,
    val slug: String?,

    val description: String?,
    val titles: Titles?,
    val canonicalTitle: String?,
    val abbreviatedTitles: List<String>?,

    val averageRating: String?,
    @Embedded(prefix = "rating_")
    val ratingFrequencies: RatingFrequencies?,
    val userCount: Int?,
    val favoritesCount: Int?,
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
    val posterImage: DBImage?,
    @Embedded(prefix = "cover_")
    val coverImage: DBImage?,

    val totalLength: Int?,
    val episodeCount: Int?,
    val episodeLength: Int?,
    val youtubeVideoId: String?,
    val subtype: AnimeSubtype?,
) : Parcelable
