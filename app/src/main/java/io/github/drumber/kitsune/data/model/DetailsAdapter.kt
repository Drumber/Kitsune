package io.github.drumber.kitsune.data.model

import android.content.Context
import android.os.Parcelable
import io.github.drumber.kitsune.R
import io.github.drumber.kitsune.data.model.resource.Titles
import io.github.drumber.kitsune.data.model.resource.anime.AgeRating
import io.github.drumber.kitsune.data.model.resource.anime.Anime
import io.github.drumber.kitsune.data.model.resource.anime.Status
import io.github.drumber.kitsune.data.model.resource.manga.Manga
import io.github.drumber.kitsune.preference.AppearancePref
import io.github.drumber.kitsune.preference.TitlesPref
import io.github.drumber.kitsune.util.originalOrDown
import io.github.drumber.kitsune.util.smallOrHigher
import kotlinx.parcelize.Parcelize

sealed class DetailsAdapter(
    val title: String,
    val description: String,
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
): Parcelable {

    @Parcelize
    class AnimeDetails(val anime: Anime): DetailsAdapter(
        title = getTitle(anime.titles, anime.canonicalTitle),
        description = anime.description.orEmpty(),
        avgRating = anime.averageRating.orEmpty(),
        userCount = anime.userCount.orNull(),
        favoriteCount = anime.favoritesCount.orNull(),
        popularityRank = anime.popularityRank.orNull(),
        ratingRank = anime.ratingRank.orNull(),
        ageRating = anime.ageRating,
        ageRatingGuide = anime.ageRatingGuide,
        subtype = anime.subtype?.name.orEmpty(),
        status = anime.status,
        tba = anime.tba,
        posterImage = anime.posterImage?.smallOrHigher(),
        coverImage = anime.coverImage?.originalOrDown()
    ), Parcelable

    @Parcelize
    class MangaDetails(val manga: Manga): DetailsAdapter(
        title = getTitle(manga.titles, manga.canonicalTitle),
        description = manga.description.orEmpty(),
        avgRating = manga.averageRating.orEmpty(),
        userCount = manga.userCount.orNull(),
        favoriteCount = manga.favoritesCount.orNull(),
        popularityRank = manga.popularityRank.orNull(),
        ratingRank = manga.ratingRank.orNull(),
        ageRating = manga.ageRating,
        ageRatingGuide = manga.ageRatingGuide,
        subtype = manga.subtype?.name.orEmpty(),
        status = manga.status,
        tba = manga.tba,
        posterImage = manga.posterImage?.smallOrHigher(),
        coverImage = manga.coverImage?.originalOrDown()
    ), Parcelable

}

private fun getTitle(title: Titles?, canonical: String?): String {
    val nf = "<No title found>"
    return when(AppearancePref.titles) {
        TitlesPref.Canoncial -> canonical ?: nf
        TitlesPref.Romanized -> title?.enJp ?: canonical ?: nf
        TitlesPref.English -> title?.en ?: canonical ?: nf
    }
}

private inline fun Int?.orNull() = this ?: 0

private inline fun Status?.asString(context: Context): String {
    return when(this) {
        Status.current -> context.getString(R.string.status_current)
        Status.finished -> context.getString(R.string.status_finished)
        Status.tba -> context.getString(R.string.status_tba)
        Status.unreleased -> context.getString(R.string.status_unreleased)
        Status.upcoming -> context.getString(R.string.status_upcoming)
        null -> context.getString(R.string.no_information)
    }
}
