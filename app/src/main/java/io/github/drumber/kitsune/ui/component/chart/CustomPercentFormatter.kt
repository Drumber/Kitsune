package io.github.drumber.kitsune.ui.component.chart

import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.formatter.ValueFormatter
import java.text.DecimalFormat

class CustomPercentFormatter : ValueFormatter() {

    private val format = DecimalFormat("##%").apply {
        multiplier = 1
    }

    override fun getFormattedValue(value: Float): String {
        return format.format(value)
    }

    override fun getPieLabel(value: Float, pieEntry: PieEntry?): String {
        return getFormattedValue(value)
    }

}