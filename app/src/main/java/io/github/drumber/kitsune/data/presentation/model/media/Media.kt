package io.github.drumber.kitsune.data.presentation.model.media

import android.content.Context
import androidx.annotation.StringRes
import io.github.drumber.kitsune.R
import io.github.drumber.kitsune.data.common.Image
import io.github.drumber.kitsune.data.common.Titles
import io.github.drumber.kitsune.data.common.en
import io.github.drumber.kitsune.data.common.enJp
import io.github.drumber.kitsune.data.common.jaJp
import io.github.drumber.kitsune.data.common.media.AgeRating
import io.github.drumber.kitsune.data.common.media.MediaType
import io.github.drumber.kitsune.data.common.media.RatingFrequencies
import io.github.drumber.kitsune.data.common.media.ReleaseStatus
import io.github.drumber.kitsune.data.presentation.model.media.category.Category
import io.github.drumber.kitsune.data.presentation.model.media.production.AnimeProductionRole
import io.github.drumber.kitsune.data.presentation.model.media.relationship.MediaRelationship
import io.github.drumber.kitsune.data.presentation.model.user.FavoriteItem
import io.github.drumber.kitsune.shared.formatDate
import io.github.drumber.kitsune.shared.parseDate
import io.github.drumber.kitsune.shared.toCalendar
import io.github.drumber.kitsune.util.DataUtil
import io.github.drumber.kitsune.util.TimeUtil
import io.github.drumber.kitsune.util.extensions.format
import java.util.Calendar

sealed class Media : FavoriteItem {
    abstract val id: String
    abstract val slug: String?

    abstract val description: String?
    abstract val titles: Titles?
    abstract val canonicalTitle: String?
    abstract val abbreviatedTitles: List<String>?

    abstract val averageRating: String?
    abstract val ratingFrequencies: RatingFrequencies?
    abstract val userCount: Int?
    abstract val favoritesCount: Int?
    abstract val popularityRank: Int?
    abstract val ratingRank: Int?

    abstract val startDate: String?
    abstract val endDate: String?
    abstract val nextRelease: String?
    abstract val tba: String?
    abstract val status: ReleaseStatus?

    abstract val ageRating: AgeRating?
    abstract val ageRatingGuide: String?
    abstract val nsfw: Boolean?

    abstract val posterImage: Image?
    abstract val coverImage: Image?

    abstract val totalLength: Int?

    abstract val categories: List<Category>?
    abstract val mediaRelationships: List<MediaRelationship>?

    //********************************************************************************************//

    abstract val mediaType: MediaType

    val title get() = DataUtil.getTitle(titles, canonicalTitle)

    val titleEn get() = titles?.en

    val titleEnJp get() = titles?.enJp

    val titleJaJp get() = titles?.jaJp

    val abbreviatedTitlesFormatted get() = abbreviatedTitles?.joinToString(", ")

    val avgRatingFormatted get() = averageRating?.tryFormatDouble()

    val posterImageUrl get() = posterImage?.smallOrHigher()

    val coverImageUrl get() = coverImage?.originalOrDown()

    val subtypeFormatted
        get() = when (this) {
            is Anime -> subtype
            is Manga -> subtype
        }?.name.orEmpty().replaceFirstChar(Char::titlecase)

    val publishingYear: Int?
        get() = startDate?.takeIf { it.isNotBlank() }
            ?.parseDate()?.toCalendar()
            ?.get(Calendar.YEAR)

    fun publishingYearText(context: Context): String {
        val publishingYear = publishingYear
        return when {
            publishingYear != null -> publishingYear.toString()
            status == ReleaseStatus.TBA -> context.getString(R.string.status_tba)
            else -> "-"
        }
    }

    @get:StringRes
    val seasonStringRes: Int
        get() {
            val date = startDate?.parseDate()?.toCalendar()
            return when (date?.get(Calendar.MONTH)?.plus(1)) {
                12, 1, 2 -> R.string.season_winter
                in 3..5 -> R.string.season_spring
                in 6..8 -> R.string.season_summer
                in 9..11 -> R.string.season_fall
                else -> R.string.no_information
            }
        }

    val seasonYear: String
        get() = startDate?.parseDate()?.toCalendar()?.let { date ->
            val year = date.get(Calendar.YEAR)
            val month = date.get(Calendar.MONTH) + 1
            if (month == 12) {
                year + 1
            } else {
                year
            }
        }?.toString() ?: ""

    val airedText: String
        get() {
            var airedText = formatDate(startDate)
            if (!endDate.isNullOrBlank() && startDate != endDate) {
                airedText += " - ${formatDate(endDate)}"
            }
            return airedText
        }

    @get:StringRes
    val statusStringRes: Int
        get() = when (status) {
            ReleaseStatus.Current -> if (this is Anime) R.string.status_current else R.string.status_current_manga
            ReleaseStatus.Finished -> R.string.status_finished
            ReleaseStatus.TBA -> R.string.status_tba
            ReleaseStatus.Unreleased -> R.string.status_unreleased
            ReleaseStatus.Upcoming -> R.string.status_upcoming
            null -> R.string.no_information
        }

    val ageRatingText: String?
        get() {
            var ageRatingText = ageRating?.name ?: return null
            if (ageRatingGuide != null) {
                ageRatingText += " - $ageRatingGuide"
            }
            return ageRatingText
        }

    val serializationText: String? get() = (this as? Manga)?.serialization

    val chapters: String? get() = (this as? Manga)?.chapterCount?.toString()

    val volumes: String?
        get() = (this as? Manga)?.volumeCount?.let { volumes ->
            if (volumes > 0) {
                volumes.toString()
            } else {
                null
            }
        }

    val episodes: String? get() = (this as? Anime)?.episodeCount?.toString()

    val episodeOrChapterCount
        get() = (this as? Anime)?.episodeCount ?: (this as? Manga)?.chapterCount

    fun lengthText(context: Context): String? {
        if (this is Anime) {
            val count = episodeCount
            val length = episodeLength ?: return null
            val lengthEachText = context.getString(R.string.data_length_each, length)
            return if (count == null) {
                lengthEachText
            } else {
                val minutes = count * length.toLong()
                val durationText = TimeUtil.timeToHumanReadableFormat(minutes * 60, context)
                if (count > 1) {
                    context.getString(
                        R.string.data_length_total,
                        durationText
                    ) + " ($lengthEachText)"
                } else {
                    durationText
                }
            }
        }
        return null
    }

    val trailerUrl: String?
        get() = if (this is Anime && !youtubeVideoId.isNullOrBlank()) {
            "https://www.youtube.com/watch?v=$youtubeVideoId"
        } else null

    val trailerCoverUrl: String?
        get() = if (this is Anime && !youtubeVideoId.isNullOrBlank()) {
            "https://img.youtube.com/vi/$youtubeVideoId/mqdefault.jpg"
        } else null

    fun getProducer(role: AnimeProductionRole): String? {
        return (this as? Anime)?.animeProduction?.filter { it.role == role }
            ?.mapNotNull { it.producer?.name }
            ?.distinct()
            ?.joinToString(", ")
    }

    fun hasStreamingLinks() = this is Anime && !streamingLinks.isNullOrEmpty()

    fun hasMediaRelationships() = !mediaRelationships.isNullOrEmpty()

    fun hasRatingFrequencies() = ratingFrequencies != null

    private fun formatDate(dateString: String?): String {
        return dateString?.takeIf { it.isNotBlank() }?.parseDate()?.formatDate() ?: ""
    }

    private fun String?.tryFormatDouble() = this?.toDoubleOrNull()?.format()
}