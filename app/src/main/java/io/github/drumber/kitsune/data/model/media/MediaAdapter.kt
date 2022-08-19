package io.github.drumber.kitsune.data.model.media

import android.content.Context
import android.os.Parcelable
import io.github.drumber.kitsune.R
import io.github.drumber.kitsune.data.model.mediarelationship.RelationshipRole
import io.github.drumber.kitsune.data.model.mediarelationship.getString
import io.github.drumber.kitsune.data.model.production.AnimeProductionRole
import io.github.drumber.kitsune.util.*
import io.github.drumber.kitsune.util.extensions.format
import kotlinx.parcelize.Parcelize
import java.util.*

/**
 * Adapter class for representing media attributes to the UI layer.
 */
@Parcelize
class MediaAdapter(
    val media: BaseMedia,
    /** Relationship role that this media has to a another media. */
    val ownRelationshipRole: RelationshipRole? = null
) : Parcelable {

    companion object {
        fun fromMedia(media: Media, ownRelationshipRole: RelationshipRole? = null) = when (media) {
            is Anime -> MediaAdapter(media, ownRelationshipRole)
            is Manga -> MediaAdapter(media, ownRelationshipRole)
            else -> throw IllegalStateException("Unknown media subclass: ${media::class.java}")
        }
    }

    fun isAnime() = media is Anime

    val id get() = media.id

    val title get() = DataUtil.getTitle(media.titles, media.canonicalTitle)

    val titles get() = media.titles ?: Titles(null, null, null)

    val abbreviatedTitles get() = media.abbreviatedTitles?.joinToString(", ")

    val description get() = media.description.orEmpty()

    val avgRating get() = media.averageRating?.tryFormatDouble()

    val ratingRank get() = media.ratingRank

    val popularityRank get() = media.popularityRank

    val tba get() = media.tba

    val categories get() = media.categories

    val posterImage get() = media.posterImage?.smallOrHigher()

    val coverImage get() = media.coverImage?.originalOrDown()

    val subtype
        get() = when (media) {
            is Anime -> media.subtype
            is Manga -> media.subtype
        }?.name.orEmpty().replaceFirstChar(Char::titlecase)

    fun ownRelationshipRoleText(context: Context): String? {
        return ownRelationshipRole?.getString(context)
    }

    val publishingYear: String
        get() = if (!media.startDate.isNullOrBlank()) {
            media.startDate!!.toDate().get(Calendar.YEAR).toString()
        } else "?"

    fun season(context: Context): String {
        val date = media.startDate?.toDate()
        val stringRes = when (date?.get(Calendar.MONTH)?.plus(1)) {
            in arrayOf(12, 1, 2) -> R.string.season_winter
            in 3..5 -> R.string.season_spring
            in 6..8 -> R.string.season_summer
            in 9..11 -> R.string.season_fall
            else -> R.string.no_information
        }
        return context.getString(stringRes)
    }

    val seasonYear: String
        get() {
            val date = media.startDate?.toDate()
            return date?.let {
                val year = date.get(Calendar.YEAR)
                val month = date.get(Calendar.MONTH) + 1
                if (month == 12) {
                    year + 1
                } else {
                    year
                }
            }?.toString() ?: ""
        }

    val airedText: String
        get() {
            var airedText = formatDate(media.startDate)
            if (!media.endDate.isNullOrBlank() && media.startDate != media.endDate) {
                airedText += " - ${formatDate(media.endDate)}"
            }
            return airedText
        }

    private fun formatDate(dateString: String?): String {
        return if (!dateString.isNullOrBlank()) {
            dateString.toDate().formatDate()
        } else {
            ""
        }
    }

    fun statusText(context: Context): String {
        val stringRes = when (media.status) {
            Status.Current -> if (isAnime()) R.string.status_current else R.string.status_current_manga
            Status.Finished -> R.string.status_finished
            Status.TBA -> R.string.status_tba
            Status.Unreleased -> R.string.status_unreleased
            Status.Upcoming -> R.string.status_upcoming
            null -> R.string.no_information
        }
        return context.getString(stringRes)
    }

    val ageRatingText: String?
        get() {
            var ageRatingText = media.ageRating?.name ?: return null
            if (media.ageRatingGuide != null) {
                ageRatingText += " - ${media.ageRatingGuide}"
            }
            return ageRatingText
        }

    val serialization: String? get() = (media as? Manga)?.serialization

    val chapters: String? get() = (media as? Manga)?.chapterCount?.toString()

    val volumes: String?
        get() = (media as? Manga)?.volumeCount?.let { volumes ->
            if (volumes > 0) {
                volumes.toString()
            } else {
                null
            }
        }

    val episodes: String? get() = (media as? Anime)?.episodeCount?.toString()

    val episodeOrChapterCount get() = (media as? Anime)?.episodeCount ?: (media as? Manga)?.chapterCount

    fun lengthText(context: Context): String? {
        if (media is Anime) {
            val count = media.episodeCount
            val length = media.episodeLength ?: return null
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
        get() = if (media is Anime && !media.youtubeVideoId.isNullOrBlank()) {
            "https://www.youtube.com/watch?v=${media.youtubeVideoId}"
        } else null

    val trailerCoverUrl: String?
        get() = if (media is Anime && !media.youtubeVideoId.isNullOrBlank()) {
            "https://img.youtube.com/vi/${media.youtubeVideoId}/mqdefault.jpg"
        } else null

    fun getProducer(role: AnimeProductionRole): String? {
        return (media as? Anime)?.animeProduction?.filter { it.role == role }
            ?.mapNotNull { it.producer?.name }
            ?.distinct()
            ?.joinToString(", ")
    }

    fun hasStreamingLinks() = media is Anime && !media.streamingLinks.isNullOrEmpty()

    fun hasMediaRelationships() = !media.mediaRelationships.isNullOrEmpty()

    fun hasRatingFrequencies() = media.ratingFrequencies != null

    private fun String?.tryFormatDouble() = this?.toDoubleOrNull()?.format()

}
