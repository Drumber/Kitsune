package io.github.drumber.kitsune.util

import android.content.Context
import io.github.drumber.kitsune.R
import io.github.drumber.kitsune.util.extensions.formatDate
import io.github.drumber.kitsune.util.extensions.toDate
import java.text.SimpleDateFormat

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

}