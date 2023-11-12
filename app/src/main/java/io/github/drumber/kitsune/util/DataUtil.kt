package io.github.drumber.kitsune.util

import android.content.Context
import io.github.drumber.kitsune.R
import io.github.drumber.kitsune.domain.model.common.media.Titles
import io.github.drumber.kitsune.domain.model.common.media.en
import io.github.drumber.kitsune.domain.model.common.media.enJp
import io.github.drumber.kitsune.domain.model.common.media.jaJp
import io.github.drumber.kitsune.domain.model.infrastructure.user.TitleLanguagePreference
import io.github.drumber.kitsune.preference.KitsunePref
import java.text.SimpleDateFormat
import java.util.Calendar

object DataUtil {

    @JvmStatic
    fun formatDate(dateString: String?) = dateString?.parseDate()?.formatDate(SimpleDateFormat.LONG)

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
        return joinDate?.parseDate()?.let { dateJoined ->
            val diffMillis = Calendar.getInstance().timeInMillis - dateJoined.time
            val differenceString = TimeUtil.roundTime(diffMillis / 1000, context)
            "${dateJoined.formatDate(SimpleDateFormat.LONG)} " +
                    "(${context.getString(R.string.profile_data_join_date_ago, differenceString)})"
        }
    }

    @JvmStatic
    fun getTitle(title: Titles?, canonical: String?): String? {
        return when (KitsunePref.titles) {
            TitleLanguagePreference.Canonical -> canonical.nb()
                ?: title?.enJp.nb() ?: title?.en.nb() ?: title?.jaJp
            TitleLanguagePreference.Romanized -> title?.enJp.nb()
                ?: canonical.nb() ?: title?.en.nb() ?: title?.jaJp
            TitleLanguagePreference.English -> title?.en.nb()
                ?: canonical.nb() ?: title?.enJp.nb() ?: title?.jaJp
        }
    }

    /**
     * Maps blank strings to null.
     */
    private fun String?.nb() = if (this.isNullOrBlank()) null else this

}