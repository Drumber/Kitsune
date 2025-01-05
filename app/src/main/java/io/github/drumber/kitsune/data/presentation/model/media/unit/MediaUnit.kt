package io.github.drumber.kitsune.data.presentation.model.media.unit

import android.content.Context
import androidx.annotation.PluralsRes
import androidx.annotation.StringRes
import io.github.drumber.kitsune.data.common.Image
import io.github.drumber.kitsune.data.common.Titles
import io.github.drumber.kitsune.shared.formatDate
import io.github.drumber.kitsune.shared.parseDate
import io.github.drumber.kitsune.util.DataUtil
import java.text.SimpleDateFormat

sealed interface MediaUnit {
    val id: String?

    val description: String?
    val titles: Titles?
    val canonicalTitle: String?

    val number: Int?
    val length: String?
    val thumbnail: Image?

    //********************************************************************************************//

    @get:StringRes
    val numberStringRes: Int

    @get:PluralsRes
    val lengthStringRes: Int

    val date: String?

    fun hasValidTitle(): Boolean {
        return DataUtil.getTitle(titles, canonicalTitle) != null &&
                !Regex("(Chapter|Episode)\\s*\\d+").matches(canonicalTitle ?: "")
    }

    fun title(context: Context) = if (hasValidTitle()) {
        DataUtil.getTitle(titles, canonicalTitle)
    } else {
        numberText(context)
    }

    fun numberText(context: Context): String? {
        return number?.let { context.getString(numberStringRes, it) }
    }

    fun formatDate(): String? {
        return date?.parseDate()?.formatDate(SimpleDateFormat.SHORT)
    }

    fun length(context: Context): String? {
        return length?.let {
            context.resources.getQuantityString(lengthStringRes, it.toInt(), it)
        }
    }
}
