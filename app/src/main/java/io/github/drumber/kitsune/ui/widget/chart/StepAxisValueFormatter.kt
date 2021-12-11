package io.github.drumber.kitsune.ui.widget.chart

import com.github.mikephil.charting.components.AxisBase
import com.github.mikephil.charting.formatter.ValueFormatter
import java.text.DecimalFormat

class StepAxisValueFormatter(
    private val startValue: Float,
    private val stepSize: Float
) : ValueFormatter() {

    override fun getAxisLabel(value: Float, axis: AxisBase?): String {
        return DecimalFormat("#.##").format(startValue + stepSize * value)
    }

}