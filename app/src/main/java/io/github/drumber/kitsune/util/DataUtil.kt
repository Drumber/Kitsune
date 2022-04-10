package io.github.drumber.kitsune.util

import android.content.Context
import io.github.drumber.kitsune.R
import io.github.drumber.kitsune.data.model.TitlesPref
import io.github.drumber.kitsune.data.model.media.Titles
import io.github.drumber.kitsune.preference.KitsunePref
import io.github.drumber.kitsune.util.extensions.formatDate
import io.github.drumber.kitsune.util.extensions.toDate
import java.text.SimpleDateFormat
import java.util.*

object DataUtil {

    @JvmStatic
    fun formatDate(dateString: String?) = dateString?.toDate()?.formatDate(SimpleDateFormat.LONG)

    @JvmStatic
    fun getGenderString(gender: String?, context: Context): String {
        return when (gender) {
            "male" -> context.getString(R.string.gender_male)
            "female" -> context.getString(R.string.gender_female)
            "secret", null -> context.getString(R.string.profile_data_private)
            else -> gender
        }
    }

    @JvmStatic
    fun formatUserJoinDate(joinDate: String?, context: Context): String? {
        return joinDate?.let { dateString ->
            val dateJoined = dateString.toDate()
            val diffMillis = Calendar.getInstance().timeInMillis - dateJoined.timeInMillis
            val differenceString = TimeUtil.roundTime(diffMillis / 1000, context)
            "${formatDate(dateString)} " +
                    "(${context.getString(R.string.profile_data_join_date_ago, differenceString)})"
        }
    }

    @JvmStatic
    fun getTitle(title: Titles?, canonical: String?): String? {
        return when (KitsunePref.titles) {
            TitlesPref.Canonical -> canonical.nb()
                ?: title?.enJp.nb() ?: title?.en.nb() ?: title?.jaJp
            TitlesPref.Romanized -> title?.enJp.nb()
                ?: canonical.nb() ?: title?.en.nb() ?: title?.jaJp
            TitlesPref.English -> title?.en.nb()
                ?: canonical.nb() ?: title?.enJp.nb() ?: title?.jaJp
        }
    }

    /**
     * Maps blank strings to null.
     */
    private fun String?.nb() = if (this.isNullOrBlank()) null else this

}