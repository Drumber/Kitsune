package io.github.drumber.kitsune.util

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone

const val DATE_FORMAT_ISO = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'"

fun String.parseDate(
    format: String = "yyyy-MM-dd",
    timeZoneOfDateString: TimeZone = TimeZone.getDefault()
): Date? {
    val dateFormat = SimpleDateFormat(format, Locale.getDefault())
    dateFormat.timeZone = timeZoneOfDateString
    return dateFormat.parse(this)
}

fun String.parseUtcDate(format: String = DATE_FORMAT_ISO) =
    parseDate(format, TimeZone.getTimeZone("UTC"))

fun Date.formatDate(dateFormat: Int = SimpleDateFormat.DEFAULT): String {
    val format = SimpleDateFormat.getDateInstance(dateFormat)
    return format.format(this)
}

fun Date.formatDate(pattern: String, timeZone: TimeZone = TimeZone.getDefault()): String {
    val format = SimpleDateFormat(pattern, Locale.getDefault())
    format.timeZone = timeZone
    return format.format(this)
}

fun Date.formatUtcDate(pattern: String = DATE_FORMAT_ISO) =
    formatDate(pattern, TimeZone.getTimeZone("UTC"))

fun Calendar.formatDate(dateFormat: Int = SimpleDateFormat.DEFAULT) = time.formatDate(dateFormat)

fun Calendar.formatDate(pattern: String, timeZone: TimeZone = TimeZone.getDefault()) =
    time.formatDate(pattern, timeZone)

fun Calendar.formatUtcDate(pattern: String = DATE_FORMAT_ISO) = time.formatUtcDate(pattern)

fun getLocalCalendar(): Calendar = Calendar.getInstance()

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

fun Long.toDate(): Date {
    val calendar = Calendar.getInstance()
    calendar.timeInMillis = this
    return calendar.time
}

fun Date.toCalendar(): Calendar {
    val calendar = Calendar.getInstance()
    calendar.time = this
    return calendar
}
