package io.github.drumber.kitsune.util.ui

import com.google.android.material.datepicker.CalendarConstraints
import com.google.android.material.datepicker.DateValidatorPointBackward
import com.google.android.material.datepicker.DateValidatorPointForward
import kotlinx.parcelize.Parcelize

@Parcelize
data class DateValidatorPointBetween(
    private val pointBackward: DateValidatorPointBackward,
    private val pointForward: DateValidatorPointForward
) : CalendarConstraints.DateValidator {

    companion object {
        fun between(from: Long, before: Long) = DateValidatorPointBetween(
            DateValidatorPointBackward.before(before),
            DateValidatorPointForward.from(from)
        )

        fun nowAndFrom(from: Long) = DateValidatorPointBetween(
            DateValidatorPointBackward.now(),
            DateValidatorPointForward.from(from)
        )
    }

    override fun isValid(date: Long): Boolean {
        return pointBackward.isValid(date) && pointForward.isValid(date)
    }

}