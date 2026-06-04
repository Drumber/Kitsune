package io.github.drumber.kitsune.ui.details

import android.content.Context
import android.view.View
import androidx.appcompat.widget.PopupMenu
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import io.github.drumber.kitsune.R
import io.github.drumber.kitsune.data.presentation.getStringRes
import io.github.drumber.kitsune.data.presentation.model.media.Media
import io.github.drumber.kitsune.data.source.local.user.model.LocalRatingSystemPreference
import io.github.drumber.kitsune.databinding.FragmentDetailsBinding
import io.github.drumber.kitsune.preference.KitsunePref
import io.github.drumber.kitsune.ui.component.chart.BarChartStyle
import io.github.drumber.kitsune.ui.component.chart.BarChartStyle.applyStyle
import io.github.drumber.kitsune.ui.component.chart.StepAxisValueFormatter
import io.github.drumber.kitsune.util.rating.RatingFrequenciesUtil.calculateAverageRating
import io.github.drumber.kitsune.util.rating.RatingFrequenciesUtil.transformToRatingSystem
import io.github.drumber.kitsune.util.rating.RatingSystemUtil.convertFrom
import io.github.drumber.kitsune.util.rating.RatingSystemUtil.stepSize
import java.text.NumberFormat
import kotlin.math.roundToInt

/**
 * Encapsulates the rating chart section of [DetailsFragment]: rendering the rating frequency bar
 * chart and the rating-system selection menu.
 */
class DetailsRatingChartSection(
    private val binding: FragmentDetailsBinding,
    private val context: Context
) {

    fun showRatingChart(media: Media) {
        val ratings = media.ratingFrequencies ?: return
        val ratingSystem = KitsunePref.ratingChartRatingSystem

        val ratingList = ratings.transformToRatingSystem(ratingSystem)

        val chartEntries = ratingList.mapIndexed { index, value ->
            BarEntry(index.toFloat(), value.toFloat())
        }

        val dataSet = BarDataSet(chartEntries, "Ratings")
        val chartColorArray = BarChartStyle
            .getColorArray(context, R.array.ratings_chart_colors)
            .let { colorArray ->
                val colorStep = (colorArray.size.toFloat() / ratingList.size).roundToInt()
                colorArray.filterIndexed { index, _ ->
                    index % colorStep == 0
                }
            }
        dataSet.applyStyle(context, chartColorArray)

        val barData = BarData(dataSet)
        barData.applyStyle(context)

        binding.chartRatings.apply {
            data = barData
            applyStyle(context)
            setFitBars(true)
            xAxis.valueFormatter = StepAxisValueFormatter(
                ratingSystem.convertFrom(2),
                ratingSystem.stepSize()
            )
            xAxis.labelCount = ratingList.size
            invalidate()
        }

        val avgRating = ratings.calculateAverageRating(ratingSystem)
        val numberFormatter = NumberFormat.getNumberInstance()
        numberFormatter.minimumFractionDigits = 1
        numberFormatter.maximumFractionDigits = 2
        binding.tvCalculatedAverageRating.text = numberFormatter.format(avgRating)
        binding.tvCalculatedAverageRatingMax.text =
            "/ " + numberFormatter.format(ratingSystem.convertFrom(20))
    }

    fun showRatingTypeMenu(anchorView: View, currentMedia: Media?) {
        val popup = PopupMenu(context, anchorView)
        val menu = popup.menu
        val selectedRatingSystem = KitsunePref.ratingChartRatingSystem

        LocalRatingSystemPreference.entries.forEach {
            val menuItem = menu.add(1, it.ordinal, it.ordinal, it.getStringRes())
            menuItem.isChecked = selectedRatingSystem == it
            menuItem.setOnMenuItemClickListener { _ ->
                KitsunePref.ratingChartRatingSystem = it
                currentMedia?.let { mediaAdapter -> showRatingChart(mediaAdapter) }
                true
            }
        }

        menu.setGroupCheckable(1, true, true)
        popup.show()
    }
}
