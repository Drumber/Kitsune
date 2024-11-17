package io.github.drumber.kitsune.util

import android.content.Context
import io.github.drumber.kitsune.R
import io.github.drumber.kitsune.data.common.Titles
import io.github.drumber.kitsune.data.common.en
import io.github.drumber.kitsune.data.common.enJp
import io.github.drumber.kitsune.data.common.enUs
import io.github.drumber.kitsune.data.common.jaJp
import io.github.drumber.kitsune.data.source.local.user.model.LocalTitleLanguagePreference
import io.github.drumber.kitsune.preference.KitsunePref
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

object DataUtil {

    @JvmStatic
    fun formatDate(dateString: String?) = dateString?.parseDate()?.formatDate(SimpleDateFormat.LONG)

    @JvmStatic
    fun getGenderString(gender: String?, context: Context): String {
        return when (gender) {
            "male" -> context.getString(R.string.profile_gender_male)
            "female" -> context.getString(R.string.profile_gender_female)
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
            LocalTitleLanguagePreference.Canonical -> canonical.nb()
                ?: title?.enJp.nb() ?: title?.en.nb() ?: title?.enUs.nb() ?: title?.jaJp
            LocalTitleLanguagePreference.Romanized -> title?.enJp.nb()
                ?: canonical.nb() ?: title?.en.nb() ?: title?.enUs.nb() ?: title?.jaJp
            LocalTitleLanguagePreference.English -> title?.en.nb()
                ?: title?.enUs.nb() ?: canonical.nb() ?: title?.enJp.nb() ?: title?.jaJp
        }
    }

    @JvmStatic
    fun <T> Map<String, T>.mapLanguageCodesToDisplayName(includeCountry: Boolean = true): Map<String, T> {
        return mapKeys {
            val locale = Locale.forLanguageTag(it.key.replace('_', '-'))
            if (includeCountry && it.key.lowercase().split('_').toSet().size > 1)
                locale.displayName
            else
                locale.displayLanguage
        }
    }

    /**
     * Maps blank strings to null.
     */
    private fun String?.nb() = if (this.isNullOrBlank()) null else this

}