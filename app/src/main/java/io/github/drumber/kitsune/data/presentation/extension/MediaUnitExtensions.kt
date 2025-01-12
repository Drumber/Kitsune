package io.github.drumber.kitsune.data.presentation.extension

import android.content.Context
import androidx.annotation.PluralsRes
import androidx.annotation.StringRes
import io.github.drumber.kitsune.R
import io.github.drumber.kitsune.data.presentation.model.media.unit.Chapter
import io.github.drumber.kitsune.data.presentation.model.media.unit.Episode
import io.github.drumber.kitsune.data.presentation.model.media.unit.MediaUnit
import io.github.drumber.kitsune.shared.formatDate
import io.github.drumber.kitsune.shared.parseDate
import io.github.drumber.kitsune.util.DataUtil
import java.text.SimpleDateFormat

@get:StringRes
val MediaUnit.numberStringRes: Int
    get() = when (this) {
        is Episode -> R.string.unit_episode
        is Chapter -> R.string.unit_chapter
    }

@get:PluralsRes
val MediaUnit.lengthStringRes: Int
    get() = when (this) {
        is Episode -> R.plurals.duration_minutes
        is Chapter -> R.plurals.unit_pages
    }

fun MediaUnit.numberText(context: Context): String? {
    return number?.let { context.getString(numberStringRes, it) }
}

fun MediaUnit.hasValidTitle(): Boolean {
    return DataUtil.getTitle(titles, canonicalTitle) != null &&
            !Regex("(Chapter|Episode)\\s*\\d+").matches(canonicalTitle ?: "")
}

fun MediaUnit.title(context: Context) = if (hasValidTitle()) {
    DataUtil.getTitle(titles, canonicalTitle)
} else {
    numberText(context)
}

fun MediaUnit.formatDate(): String? {
    return date?.parseDate()?.formatDate(SimpleDateFormat.SHORT)
}

fun MediaUnit.length(context: Context): String? {
    return length?.let {
        context.resources.getQuantityString(lengthStringRes, it.toInt(), it)
    }
}