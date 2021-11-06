package io.github.drumber.kitsune.util

import android.content.Context
import io.github.drumber.kitsune.R
import java.math.RoundingMode
import kotlin.math.roundToInt
import kotlin.time.Duration
import kotlin.time.DurationUnit
import kotlin.time.ExperimentalTime

object TimeUtil {

    @OptIn(ExperimentalTime::class)
    fun timeToHumanReadableFormat(
        timeSeconds: Long,
        context: Context,
        includeSeconds: Boolean = false
    ): String {
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
        if (includeSeconds && seconds > 0) {
            parts += res.getQuantityString(R.plurals.duration_seconds, seconds.toInt(), seconds)
        }

        return parts.joinToString(", ")
    }

    @OptIn(ExperimentalTime::class)
    fun roundTime(timeSeconds: Long, context: Context, decimalPlaces: Int = 1): String {
        val res = context.resources
        val time = Duration.seconds(timeSeconds)

        return when {
            time.inWholeYears > 0 -> {
                val years = time.toYearsDouble().round(decimalPlaces)
                res.getQuantityString(R.plurals.duration_years, years.roundToInt(), years)
            }
            time.inWholeMonths > 0 -> {
                val months = time.toMonthsDouble().round(decimalPlaces)
                res.getQuantityString(R.plurals.duration_months, months.roundToInt(), months)
            }
            time.inWholeDays > 0 -> {
                val days = time.toDouble(DurationUnit.DAYS).round(decimalPlaces)
                res.getQuantityString(R.plurals.duration_days, days.roundToInt(), days)
            }
            time.inWholeHours > 0 -> {
                val hours = time.toDouble(DurationUnit.HOURS).round(decimalPlaces)
                res.getQuantityString(R.plurals.duration_hours, hours.roundToInt(), hours)
            }
            time.inWholeMinutes > 0 -> {
                val minutes = time.toDouble(DurationUnit.MINUTES).round(decimalPlaces)
                res.getQuantityString(R.plurals.duration_minutes, minutes.roundToInt(), minutes)
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
    private fun monthsToDuration(value: Long) = Duration.days(value * 30)

    @ExperimentalTime
    private fun yearsToDuration(value: Long) = Duration.days(value * 365)

    @ExperimentalTime
    private fun Duration.toMonthsDouble() = this.toDouble(DurationUnit.DAYS) / 30.0

    @ExperimentalTime
    private fun Duration.toYearsDouble() = this.toDouble(DurationUnit.DAYS) / 365.0

    private fun Double.round(decimalPlaces: Int) = this.toBigDecimal()
        .setScale(decimalPlaces, RoundingMode.HALF_UP)
        .toDouble()

}