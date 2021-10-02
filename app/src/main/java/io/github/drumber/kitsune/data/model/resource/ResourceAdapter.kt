package io.github.drumber.kitsune.data.model.resource

import android.content.Context
import android.os.Parcelable
import io.github.drumber.kitsune.R
import io.github.drumber.kitsune.preference.KitsunePref
import io.github.drumber.kitsune.preference.TitlesPref
import io.github.drumber.kitsune.util.originalOrDown
import io.github.drumber.kitsune.util.smallOrHigher
import io.github.drumber.kitsune.util.toDate
import kotlinx.parcelize.Parcelize
import java.text.SimpleDateFormat
import java.util.*

sealed class ResourceAdapter(
    val title: String,
    val titles: Titles,
    val description: String,
    val startDate: String?,
    val endDate: String?,
    val avgRating: String,
    val userCount: Int,
    val favoriteCount: Int,
    val popularityRank: Int,
    val ratingRank: Int,
    val ageRating: AgeRating?,
    val ageRatingGuide: String?,
    val subtype: String,
    val status: Status?,
    val tba: String?,
    val posterImage: String?,
    val coverImage: String?
) : Parcelable {

    val publishingYear: String
        get() = if (!startDate.isNullOrBlank()) {
            startDate.toDate().get(Calendar.YEAR).toString()
        } else "?"

    fun season(context: Context): String {
        val date = startDate?.toDate()
        val stringRes = when (date?.get(Calendar.MONTH)?.plus(1)) {
            in arrayOf(12, 1, 2) -> R.string.season_winter
            in 3..5 -> R.string.season_spring
            in 6..8 -> R.string.season_summer
            in 9..11 -> R.string.season_fall
            else -> R.string.no_information
        }
        return context.getString(stringRes)
    }

    val seasonYear: String get() {
        val date = startDate?.toDate()
        return date?.let {
            val year = date.get(Calendar.YEAR)
            val month = date.get(Calendar.MONTH) + 1
            if (month == 12) {
                year + 1
            } else {
                year
            }
        }?.toString() ?: "?"
    }

    val airedText: String get() {
        var airedText = formatDate(startDate)
        if(!endDate.isNullOrBlank()) {
            airedText += " - ${formatDate(endDate)}"
        }
        return airedText
    }

    private fun formatDate(dateString: String?): String {
        return if (!dateString.isNullOrBlank()) {
            val dateFormat = SimpleDateFormat.getDateInstance(SimpleDateFormat.DEFAULT)
            dateFormat.format(dateString.toDate().time)
        } else {
            "?"
        }
    }

    fun statusText(context: Context): String {
        val stringRes = when (status) {
            Status.current -> if(isAnime()) R.string.status_current else R.string.status_current_manga
            Status.finished -> R.string.status_finished
            Status.tba -> R.string.status_tba
            Status.unreleased -> R.string.status_unreleased
            Status.upcoming -> R.string.status_upcoming
            null -> R.string.no_information
        }
        return context.getString(stringRes)
    }

    fun isAnime() = this is AnimeResource

    @Parcelize
    class AnimeResource(val anime: Anime) : ResourceAdapter(
        title = getTitle(anime.titles, anime.canonicalTitle),
        titles = anime.titles.require(),
        description = anime.description.orEmpty(),
        startDate = anime.startDate,
        endDate = anime.endDate,
        avgRating = anime.averageRating.orEmpty(),
        userCount = anime.userCount.orNull(),
        favoriteCount = anime.favoritesCount.orNull(),
        popularityRank = anime.popularityRank.orNull(),
        ratingRank = anime.ratingRank.orNull(),
        ageRating = anime.ageRating,
        ageRatingGuide = anime.ageRatingGuide,
        subtype = anime.subtype?.name.orEmpty().replaceFirstChar(Char::titlecase),
        status = anime.status,
        tba = anime.tba,
        posterImage = anime.posterImage?.smallOrHigher(),
        coverImage = anime.coverImage?.originalOrDown()
    ), Parcelable

    @Parcelize
    class MangaResource(val manga: Manga) : ResourceAdapter(
        title = getTitle(manga.titles, manga.canonicalTitle),
        titles = manga.titles.require(),
        description = manga.description.orEmpty(),
        startDate = manga.startDate,
        endDate = manga.endDate,
        avgRating = manga.averageRating.orEmpty(),
        userCount = manga.userCount.orNull(),
        favoriteCount = manga.favoritesCount.orNull(),
        popularityRank = manga.popularityRank.orNull(),
        ratingRank = manga.ratingRank.orNull(),
        ageRating = manga.ageRating,
        ageRatingGuide = manga.ageRatingGuide,
        subtype = manga.subtype?.name.orEmpty().replaceFirstChar(Char::titlecase),
        status = manga.status,
        tba = manga.tba,
        posterImage = manga.posterImage?.smallOrHigher(),
        coverImage = manga.coverImage?.originalOrDown()
    ), Parcelable

    companion object {
        fun fromResource(resource: Resource) = when (resource) {
            is Anime -> AnimeResource(resource)
            is Manga -> MangaResource(resource)
        }
    }

}

private fun getTitle(title: Titles?, canonical: String?): String {
    val nf = "<No title found>"
    return when (KitsunePref.titles) {
        TitlesPref.Canoncial -> canonical ?: nf
        TitlesPref.Romanized -> title?.enJp ?: canonical ?: nf
        TitlesPref.English -> title?.en ?: canonical ?: nf
    }
}

private fun Titles?.require(): Titles {
    return this ?: Titles(null, null, null)
}

private inline fun Int?.orNull() = this ?: 0
