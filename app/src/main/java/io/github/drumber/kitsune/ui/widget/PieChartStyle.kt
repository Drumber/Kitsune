package io.github.drumber.kitsune.ui.widget

import android.content.Context
import androidx.annotation.StringRes
import com.github.mikephil.charting.animation.Easing
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.formatter.PercentFormatter
import io.github.drumber.kitsune.R
import io.github.drumber.kitsune.util.extensions.getColor

object PieChartStyle {

    const val STATS_MAX_ELEMENTS = 7
    const val ANIMATION_DURATION = 1000

    fun PieChart.applyStyle(
        c: Context,
        @StringRes centerTextResId: Int? = null,
        showLegend: Boolean = false
    ) {
        val theme = c.theme

        setUsePercentValues(false)
        description.isEnabled = false
        setExtraOffsets(5f, 5f, 5f, 5f)

        enableScroll()
        isRotationEnabled = false

        isDrawHoleEnabled = true
        setHoleColor(theme.getColor(android.R.color.transparent))
        holeRadius = 55f
        setTransparentCircleAlpha(50)

        setCenterTextColor(theme.getColor(R.attr.colorOnSurface))
        if (centerTextResId != null) {
            centerText = c.getString(centerTextResId)
        }

        isHighlightPerTapEnabled = true

        setNoDataTextColor(theme.getColor(R.attr.colorControlNormal))
        setEntryLabelColor(theme.getColor(R.attr.colorOnSurface))

        animateY(ANIMATION_DURATION, Easing.EaseInOutQuad)

        legend.apply {
            form = Legend.LegendForm.CIRCLE
            verticalAlignment = Legend.LegendVerticalAlignment.BOTTOM
            horizontalAlignment = Legend.LegendHorizontalAlignment.RIGHT
            orientation = Legend.LegendOrientation.VERTICAL
            setDrawInside(false)
            textColor = theme.getColor(R.attr.colorOnSurface)
            yEntrySpace = 2f
            yOffset = 0f
            xOffset = 0f
            isEnabled = showLegend
        }
    }

    fun PieDataSet.applyStyle(c: Context) {
        setDrawIcons(false)
        sliceSpace = 0f
        selectionShift = 5f
        colors = getChartColors(c)
    }

    fun PieData.applyStyle(c: Context) {
        setValueFormatter(PercentFormatter())
        setValueTextSize(11f)
        setValueTextColor(c.theme.getColor(R.attr.colorOnSurface))
    }

    private fun getChartColors(c: Context): List<Int> {
        val colors = c.resources.obtainTypedArray(R.array.stats_chart_colors)
        val list = mutableListOf<Int>()
        for (i in 0 until colors.length()) {
            list += colors.getColor(i, 0)
        }
        colors.recycle()
        return list
    }

}