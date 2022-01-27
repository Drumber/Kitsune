package io.github.drumber.kitsune.ui.widget.chart

import android.content.Context
import androidx.annotation.ArrayRes
import com.github.mikephil.charting.data.BaseDataSet
import com.github.mikephil.charting.data.ChartData
import io.github.drumber.kitsune.R
import io.github.drumber.kitsune.util.extensions.getColor

abstract class BaseChartStyle {

    protected fun BaseDataSet<*>.applyBaseStyle(
        c: Context,
        colorArray: List<Int> = getColorArray(c, R.array.stats_chart_colors)
    ) {
        setDrawIcons(false)
        colors = colorArray
    }

    fun ChartData<*>.applyBaseStyle(c: Context) {
        setValueTextSize(11f)
        setValueTextColor(c.theme.getColor(R.attr.colorOnSurface))
    }

    fun getColorArray(c: Context, @ArrayRes colorArray: Int): List<Int> {
        val colors = c.resources.obtainTypedArray(colorArray)
        val list = mutableListOf<Int>()
        for (i in 0 until colors.length()) {
            list += colors.getColor(i, 0)
        }
        colors.recycle()
        return list
    }

}