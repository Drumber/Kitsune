package io.github.drumber.kitsune.util

import android.content.Context
import io.github.drumber.kitsune.R
import kotlin.time.Duration
import kotlin.time.ExperimentalTime

object DataDisplayUtil {

    @OptIn(ExperimentalTime::class)
    fun timeToHumanReadableFormat(timeSeconds: Long, context: Context): String {
        val res = context.resources
        val parts = mutableListOf<String>()

        var remaining = Duration.seconds(timeSeconds)

        val years = remaining.inWholeYears
        if (years > 0) {
            parts += res.getQuantityString(R.plurals.duration_years, years.toInt(), years)
            remaining -= yearsToDuration(years)
        }
        val months = remaining.inWholeMonths
        if (months > 0) {
            parts += res.getQuantityString(R.plurals.duration_months, months.toInt(), months)
            remaining -= monthsToDuration(months)
        }
        val days = remaining.inWholeDays
        if (days > 0) {
            parts += res.getQuantityString(R.plurals.duration_days, days.toInt(), days)
            remaining -= Duration.days(days)
        }
        val hours = remaining.inWholeHours
        if (hours > 0) {
            parts += res.getQuantityString(R.plurals.duration_hours, hours.toInt(), hours)
            remaining -= Duration.hours(hours)
        }
        val minutes = remaining.inWholeMinutes
        if (minutes > 0) {
            parts += res.getQuantityString(R.plurals.duration_minutes, minutes.toInt(), minutes)
            remaining -= Duration.minutes(minutes)
        }
        val seconds = remaining.inWholeSeconds
        if (seconds > 0) {
            parts += res.getQuantityString(R.plurals.duration_seconds, seconds.toInt(), seconds)
        }

        return parts.joinToString(", ")
    }

    @ExperimentalTime
    private val Duration.inWholeMonths
        get() = inWholeDays / 30

    @ExperimentalTime
    private val Duration.inWholeYears
        get() = inWholeDays / 365

    @ExperimentalTime
    private fun monthsToDuration(value: Long) = Duration.days(value * 30)

    @ExperimentalTime
    private fun yearsToDuration(value: Long) = Duration.days(value * 365)

}