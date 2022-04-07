package io.github.drumber.kitsune.ui.widget.chart

import com.github.mikephil.charting.formatter.LargeValueFormatter

class NonZeroLargeValueFormatter : LargeValueFormatter() {

    override fun getFormattedValue(value: Float): String {
        return if (value > 0f) {
            super.getFormattedValue(value)
        } else {
            ""
        }
    }

}