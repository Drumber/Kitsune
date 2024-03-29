package io.github.drumber.kitsune.util

import android.content.Context
import io.github.drumber.kitsune.R
import io.github.drumber.kitsune.util.extensions.format
import java.math.RoundingMode
import kotlin.math.roundToInt
import kotlin.time.Duration
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds
import kotlin.time.DurationUnit
import kotlin.time.ExperimentalTime

object TimeUtil {

    /**
     * Formats the given time in seconds to a human readable time string like:
     * `2 years, 1 month, 5 days, 1 hour, 2 minutes`
     */
    @OptIn(ExperimentalTime::class)
    fun timeToHumanReadableFormat(
        timeSeconds: Long,
        context: Context,
        includeSeconds: Boolean = false
    ): String {
        val res = context.resources
        val parts = mutableListOf<String>()

        var remaining = timeSeconds.seconds

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
            remaining -= days.days
        }
        val hours = remaining.inWholeHours
        if (hours > 0) {
            parts += res.getQuantityString(R.plurals.duration_hours, hours.toInt(), hours)
            remaining -= hours.hours
        }
        val minutes = remaining.inWholeMinutes
        if (minutes > 0) {
            parts += res.getQuantityString(R.plurals.duration_minutes, minutes.toInt(), minutes)
            remaining -= minutes.minutes
        }
        val seconds = remaining.inWholeSeconds
        if (includeSeconds && seconds > 0) {
            parts += res.getQuantityString(R.plurals.duration_seconds, seconds.toInt(), seconds)
        }

        return parts.joinToString(", ")
    }

    /**
     * Formats the given time in seconds to a rounded time string.
     *
     * Example:
     * ```
     * 1s    -> 1 second
     * 59s   -> 59 seconds
     * 60s   -> 1 minute
     * 90s   -> 1.5 minutes
     * 3600s -> 1 hour
     * ```
     */
    @OptIn(ExperimentalTime::class)
    fun roundTime(timeSeconds: Long, context: Context, decimalPlaces: Int = 1): String {
        val res = context.resources
        val time = timeSeconds.seconds

        return when {
            time.inWholeYears > 0 -> {
                val years = time.toYearsDouble().round(decimalPlaces)
                res.getQuantityString(R.plurals.duration_years, years.roundToInt(), years.format())
            }
            time.inWholeMonths > 0 -> {
                val months = time.toMonthsDouble().round(decimalPlaces)
                res.getQuantityString(R.plurals.duration_months, months.roundToInt(), months.format())
            }
            time.inWholeDays > 0 -> {
                val days = time.toDouble(DurationUnit.DAYS).round(decimalPlaces)
                res.getQuantityString(R.plurals.duration_days, days.roundToInt(), days.format())
            }
            time.inWholeHours > 0 -> {
                val hours = time.toDouble(DurationUnit.HOURS).round(decimalPlaces)
                res.getQuantityString(R.plurals.duration_hours, hours.roundToInt(), hours.format())
            }
            time.inWholeMinutes > 0 -> {
                val minutes = time.toDouble(DurationUnit.MINUTES).round(decimalPlaces)
                res.getQuantityString(R.plurals.duration_minutes, minutes.roundToInt(), minutes.format())
            }
            else -> {
                res.getQuantityString(R.plurals.duration_seconds, timeSeconds.toInt(), timeSeconds)
            }
        }
    }

    @ExperimentalTime
    private val Duration.inWholeMonths
        get() = inWholeDays / 30

    @ExperimentalTime
    private val Duration.inWholeYears
        get() = inWholeDays / 365

    @ExperimentalTime
    private fun monthsToDuration(value: Long) = (value * 30).days

    @ExperimentalTime
    private fun yearsToDuration(value: Long) = (value * 365).days

    @ExperimentalTime
    private fun Duration.toMonthsDouble() = this.toDouble(DurationUnit.DAYS) / 30.0

    @ExperimentalTime
    private fun Duration.toYearsDouble() = this.toDouble(DurationUnit.DAYS) / 365.0

    private fun Double.round(decimalPlaces: Int) = this.toBigDecimal()
        .setScale(decimalPlaces, RoundingMode.HALF_UP)
        .toDouble()

}