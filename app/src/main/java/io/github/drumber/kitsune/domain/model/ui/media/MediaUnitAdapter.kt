package io.github.drumber.kitsune.domain.model.ui.media

import android.content.Context
import io.github.drumber.kitsune.R
import io.github.drumber.kitsune.domain.model.infrastructure.image.Image
import io.github.drumber.kitsune.domain.model.infrastructure.media.Titles
import io.github.drumber.kitsune.domain.model.infrastructure.media.unit.Chapter
import io.github.drumber.kitsune.domain.model.infrastructure.media.unit.Episode
import io.github.drumber.kitsune.domain.model.infrastructure.media.unit.MediaUnit
import io.github.drumber.kitsune.util.DataUtil
import io.github.drumber.kitsune.util.formatDate
import io.github.drumber.kitsune.util.toDate
import kotlinx.parcelize.Parcelize
import java.text.SimpleDateFormat

@Parcelize
class MediaUnitAdapter(
    val unit: MediaUnit,
    override val id: String?,
    override val description: String?,
    override val titles: Titles?,
    override val canonicalTitle: String?,
    override val number: Int?,
    override val length: String?,
    override val thumbnail: Image?,
) : MediaUnit {

    val hasValidTitle
        get() = DataUtil.getTitle(titles, canonicalTitle) != null &&
                !"""(Chapter|Episode)\s*\d+""".toRegex().matches(unit.canonicalTitle ?: "")

    fun title(context: Context) = if (hasValidTitle) {
        DataUtil.getTitle(titles, canonicalTitle)
    } else {
        numberText(context)
    }

    fun numberText(context: Context): String? {
        return when (unit) {
            is Episode -> number?.let { context.getString(R.string.unit_episode, it) }
            is Chapter -> number?.let { context.getString(R.string.unit_chapter, it) }
            else -> null
        }
    }

    val date: String?
        get() {
            val dateText = when (unit) {
                is Episode -> unit.airdate
                is Chapter -> unit.published
                else -> null
            }
            return dateText?.toDate()?.formatDate(SimpleDateFormat.SHORT)
        }

    fun length(context: Context): String? {
        return when (unit) {
            is Episode -> unit.length?.let {
                context.resources.getQuantityString(R.plurals.duration_minutes, it.toInt(), it)
            }
            is Chapter -> unit.length?.let {
                context.resources.getQuantityString(R.plurals.unit_pages, it.toInt(), it)
            }
            else -> null
        }
    }

    companion object {
        fun fromMediaUnit(mediaUnit: MediaUnit) = with(mediaUnit) {
            MediaUnitAdapter(
                unit = this,
                id,
                description,
                titles,
                canonicalTitle,
                number,
                length,
                thumbnail
            )
        }
    }

}
