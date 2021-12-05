package io.github.drumber.kitsune.ui.widget.chart

import android.content.Context
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.formatter.LargeValueFormatter
import io.github.drumber.kitsune.R
import io.github.drumber.kitsune.util.extensions.getColor

object BarChartStyle : BaseChartStyle() {

    fun BarChart.applyStyle(
        c: Context
    ) {
        val theme = c.theme

        description.isEnabled = false
        enableScroll()
        isHighlightPerTapEnabled = true
        setNoDataTextColor(theme.getColor(R.attr.colorControlNormal))
        legend.isEnabled = false

        axisLeft.isEnabled = false
        axisRight.isEnabled = false
        xAxis.apply {
            position = XAxis.XAxisPosition.BOTTOM
            textColor = theme.getColor(R.attr.colorOnSurface)
            setDrawAxisLine(false)
            setDrawGridLines(false)
        }

        setPinchZoom(false)
        isDoubleTapToZoomEnabled = false
        setDrawGridBackground(false)
    }

    fun BarDataSet.applyStyle(c: Context) {
        valueFormatter = LargeValueFormatter()
        isHighlightEnabled = false
        applyBaseStyle(c, R.array.ratings_chart_colors)
    }

    fun BarData.applyStyle(c: Context) {
        applyBaseStyle(c)
    }

}