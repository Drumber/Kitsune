package io.github.drumber.kitsune.ui.component.chart

import android.content.Context
import androidx.annotation.StringRes
import com.github.mikephil.charting.animation.Easing
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import io.github.drumber.kitsune.R
import io.github.drumber.kitsune.util.extensions.getColor

object PieChartStyle : BaseChartStyle() {

    const val STATS_MAX_ELEMENTS = 7
    const val ANIMATION_DURATION = 800

    fun PieChart.applyStyle(
        c: Context,
        @StringRes centerTextResId: Int? = null
    ) {
        val theme = c.theme

        setUsePercentValues(false)
        description.isEnabled = false
        setExtraOffsets(8f, 8f, 8f, 8f)

        // Clean donut without on-slice labels. Tapping a slice reveals a floating
        // tooltip (handled by the adapter) instead of the cluttered legend list.
        isRotationEnabled = false
        isHighlightPerTapEnabled = true
        setDrawEntryLabels(false)

        isDrawHoleEnabled = true
        setHoleColor(theme.getColor(android.R.color.transparent))
        holeRadius = 68f
        transparentCircleRadius = 72f
        setTransparentCircleColor(theme.getColor(R.attr.colorOnSurface))
        setTransparentCircleAlpha(20)

        setCenterTextColor(theme.getColor(R.attr.colorOnSurface))
        setCenterTextSize(14f)
        if (centerTextResId != null) {
            centerText = c.getString(centerTextResId)
        }

        setNoDataTextColor(theme.getColor(R.attr.colorControlNormal))

        animateY(ANIMATION_DURATION, Easing.EaseInOutQuad)

        legend.isEnabled = false
    }

    fun PieDataSet.applyStyle(c: Context) {
        applyBaseStyle(c)
        sliceSpace = 2f
        selectionShift = 6f
        setDrawValues(false)
    }

    fun PieData.applyStyle(c: Context) {
        applyBaseStyle(c)
        setDrawValues(false)
    }

}