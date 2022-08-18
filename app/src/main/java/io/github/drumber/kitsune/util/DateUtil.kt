package io.github.drumber.kitsune.util

import com.google.android.material.datepicker.MaterialDatePicker
import java.text.SimpleDateFormat
import java.util.*

const val DATE_FORMAT_ISO = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'"

fun String.toDate(format: String = "yyyy-MM-dd"): Calendar {
    val date = SimpleDateFormat(format, Locale.getDefault()).parse(this)
    return Calendar.getInstance().apply {
        if (date != null) {
            time = date
        }
    }
}

fun Date.formatDate(dateFormat: Int = SimpleDateFormat.DEFAULT): String {
    val format = SimpleDateFormat.getDateInstance(dateFormat)
    return format.format(this)
}

fun Date.formatDate(pattern: String): String {
    val format = SimpleDateFormat(pattern, Locale.getDefault())
    return format.format(this)
}

fun Long.toDate(): Date {
    val calendar = Calendar.getInstance()
    calendar.timeInMillis = this
    return calendar.time
}

fun Calendar.formatDate(dateFormat: Int = SimpleDateFormat.DEFAULT) = this.time.formatDate(dateFormat)

fun getUtcCalendar(rawCalendar: Calendar? = null): Calendar {
    val calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
    if (rawCalendar == null) {
        calendar.clear()
    } else {
        calendar.timeInMillis = rawCalendar.timeInMillis
    }
    return calendar
}

fun getDayCopyInUtc(rawCalendar: Calendar): Calendar {
    val rawCalendarInUtc = getUtcCalendar(rawCalendar)
    val utcCalendar = getUtcCalendar()
    utcCalendar.set(
        rawCalendarInUtc.get(Calendar.YEAR),
        rawCalendarInUtc.get(Calendar.MONTH),
        rawCalendarInUtc.get(Calendar.DAY_OF_MONTH)
    )
    return utcCalendar
}

/**
 * Keeps only year, month and day information of the specified time in milliseconds.
 */
fun stripTimeOfUtcMillis(rawDate: Long): Long {
    val rawCalendar = getUtcCalendar()
    rawCalendar.timeInMillis = rawDate
    val sanitizedStartItem = getDayCopyInUtc(rawCalendar)
    return sanitizedStartItem.timeInMillis
}

/**
 * Keeps only year, month and day information of the specified time in milliseconds.
 */
fun Long.stripTimeUtcMillis() = stripTimeOfUtcMillis(this)

fun todayUtcMillis() = MaterialDatePicker.todayInUtcMilliseconds()
