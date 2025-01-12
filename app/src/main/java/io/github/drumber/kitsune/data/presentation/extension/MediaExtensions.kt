package io.github.drumber.kitsune.data.presentation.extension

import android.content.Context
import androidx.annotation.StringRes
import io.github.drumber.kitsune.R
import io.github.drumber.kitsune.data.common.model.media.ReleaseStatus
import io.github.drumber.kitsune.data.presentation.model.media.Anime
import io.github.drumber.kitsune.data.presentation.model.media.Media
import io.github.drumber.kitsune.shared.formatDate
import io.github.drumber.kitsune.shared.parseDate
import io.github.drumber.kitsune.shared.toCalendar
import io.github.drumber.kitsune.util.DataUtil
import io.github.drumber.kitsune.util.TimeUtil
import io.github.drumber.kitsune.util.extensions.format
import java.util.Calendar

val Media.title get() = DataUtil.getTitle(titles, canonicalTitle)

val Media.seasonYear: String
    get() = startDate?.parseDate()?.toCalendar()?.let { date ->
        val year = date.get(Calendar.YEAR)
        val month = date.get(Calendar.MONTH) + 1
        if (month == 12) {
            year + 1
        } else {
            year
        }
    }?.toString() ?: ""

@get:StringRes
val Media.seasonStringRes: Int
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

val Media.publishingYear: Int?
    get() = startDate?.takeIf { it.isNotBlank() }
        ?.parseDate()?.toCalendar()
        ?.get(Calendar.YEAR)

fun Media.publishingYearText(context: Context): String {
    val publishingYear = publishingYear
    return when {
        publishingYear != null -> publishingYear.toString()
        status == ReleaseStatus.TBA -> context.getString(R.string.status_tba)
        else -> "-"
    }
}

@get:StringRes
val Media.statusStringRes: Int
    get() = when (status) {
        ReleaseStatus.Current -> if (this is Anime) R.string.status_current else R.string.status_current_manga
        ReleaseStatus.Finished -> R.string.status_finished
        ReleaseStatus.TBA -> R.string.status_tba
        ReleaseStatus.Unreleased -> R.string.status_unreleased
        ReleaseStatus.Upcoming -> R.string.status_upcoming
        null -> R.string.no_information
    }

fun Media.lengthText(context: Context): String? {
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

val Media.airedText: String
    get() {
        var airedText = formatDate(startDate)
        if (!endDate.isNullOrBlank() && startDate != endDate) {
            airedText += " - ${formatDate(endDate)}"
        }
        return airedText
    }

val Media.avgRatingFormatted get() = averageRating?.tryFormatDouble()

private fun formatDate(dateString: String?): String {
    return dateString?.takeIf { it.isNotBlank() }?.parseDate()?.formatDate() ?: ""
}

private fun String?.tryFormatDouble() = this?.toDoubleOrNull()?.format()