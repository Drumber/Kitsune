package io.github.drumber.kitsune.ui.profile

import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.text.HtmlCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.listener.OnChartValueSelectedListener
import io.github.drumber.kitsune.R
import io.github.drumber.kitsune.data.presentation.model.user.stats.UserStatsData
import io.github.drumber.kitsune.databinding.ItemProfileStatsBinding
import io.github.drumber.kitsune.ui.component.chart.PieChartStyle
import io.github.drumber.kitsune.ui.component.chart.PieChartStyle.applyStyle
import io.github.drumber.kitsune.util.TimeUtil
import kotlin.math.roundToInt

class ProfileStatsAdapter(dataSet: List<ProfileStatsData>) :
    RecyclerView.Adapter<ProfileStatsAdapter.ProfileStatsViewHolder>() {

    companion object {
        const val POS_ANIME = 0
        const val POS_MANGA = 1
    }

    private val dataSet = dataSet.toMutableList()

    fun updateCategoryData(position: Int, data: PieDataSet) {
        if (dataSet[position].categoriesDataSet == data) return
        dataSet[position].categoriesDataSet = data
        notifyItemChanged(position)
    }

    fun updateAmountConsumedData(position: Int, data: UserStatsData.AmountConsumedData?) {
        if (dataSet[position].amountConsumedData == data) return
        dataSet[position].amountConsumedData = data
        notifyItemChanged(position)
    }

    fun setLoading(position: Int, isLoading: Boolean) {
        if (dataSet[position].isLoading == isLoading) return
        dataSet[position].isLoading = isLoading
        notifyItemChanged(position)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProfileStatsViewHolder {
        val binding = ItemProfileStatsBinding
            .inflate(LayoutInflater.from(parent.context), parent, false)
        return ProfileStatsViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ProfileStatsViewHolder, position: Int) {
        holder.bind(dataSet[position])
    }

    override fun getItemCount() = dataSet.size

    inner class ProfileStatsViewHolder(private val binding: ItemProfileStatsBinding) :
        RecyclerView.ViewHolder(binding.root) {

        init {
            binding.apply {
                pieChart.applyStyle(binding.root.context)
                progressBar.isVisible = true
            }
        }

        private val isAnime get() = bindingAdapterPosition == POS_ANIME
        private val isManga get() = bindingAdapterPosition == POS_MANGA

        private var chartCenterText: String = ""

        fun bind(dataModel: ProfileStatsData) {
            val context = binding.root.context
            updateCategoryChart(dataModel)

            binding.apply {
                progressBar.isVisible = dataModel.isLoading

                dataModel.amountConsumedData?.let { stats ->
                    if (isAnime) {
                        stats.time?.let { time ->
                            tvTimeSpent.text = context.getString(
                                R.string.profile_stats_anime_watch_time,
                                TimeUtil.roundTime(time, context, 1)
                            )

                            if (time > 0) {
                                tvTimeSpentTotal.text = context.getString(
                                    R.string.profile_stats_time_spent_total,
                                    TimeUtil.timeToHumanReadableFormat(time, context)
                                )
                            }
                        }
                    } else if (isManga) {
                        tvTimeSpent.text = stats.units?.let { chapters ->
                            context.getString(R.string.profile_stats_manga_chapters_read, chapters)
                        }
                    }

                    val completed = stats.completed
                    val percentiles = if (isAnime) {
                        stats.percentiles?.time
                    } else {
                        stats.percentiles?.units
                    }
                    val htmlText = if (completed != null && percentiles != null) {
                        context.getString(
                            R.string.profile_stats_completed,
                            completed,
                            percentiles.times(100).roundToInt().coerceIn(0..99)
                        )
                    } else {
                        null
                    }
                    tvCompleted.text = htmlText?.let {
                        HtmlCompat.fromHtml(it, HtmlCompat.FROM_HTML_MODE_LEGACY)
                    }
                }
            }
        }

        private fun updateCategoryChart(dataModel: ProfileStatsData) {
            val context = binding.root.context

            val isDataEmpty = dataModel.categoriesDataSet.let { it == null || it.values.isEmpty() }
            binding.apply {
                pieChart.isVisible = !isDataEmpty && !dataModel.isLoading
                tvCategoriesNoData.isVisible = isDataEmpty && !dataModel.isLoading
            }

            hideTooltip(animate = false)

            dataModel.categoriesDataSet?.takeUnless { isDataEmpty }?.let { set ->
                set.applyStyle(context)

                val pieData = PieData(set)
                pieData.applyStyle(context)

                chartCenterText = dataModel.title
                binding.pieChart.apply {
                    data = pieData
                    centerText = chartCenterText
                    highlightValues(null)
                    setOnChartValueSelectedListener(chartValueSelectedListener)
                    invalidate()
                }

                highlightLargestSlice(set)
            } ?: run {
                binding.pieChart.clear()
            }
        }

        private fun highlightLargestSlice(set: PieDataSet) {
            val largestIndex = (0 until set.entryCount)
                .maxByOrNull { set.getEntryForIndex(it).value }
                ?: return
            val entry = set.getEntryForIndex(largestIndex) as? PieEntry ?: return

            binding.pieChart.highlightValue(
                Highlight(largestIndex.toFloat(), 0, 0),
                false
            )
            showTooltip(entry, largestIndex)
        }

        private val chartValueSelectedListener =
            object : OnChartValueSelectedListener {
                override fun onValueSelected(e: Entry?, h: Highlight?) {
                    val entry = e as? PieEntry ?: return
                    val index = h?.x?.toInt() ?: return
                    showTooltip(entry, index)
                }

                override fun onNothingSelected() {
                    hideTooltip(animate = true)
                }
            }

        private fun showTooltip(entry: PieEntry, index: Int) {
            val context = binding.root.context
            val colors = PieChartStyle.getColorArray(context, R.array.stats_chart_colors)
            val color = colors[index % colors.size]

            binding.apply {
                viewTooltipDot.backgroundTintList = ColorStateList.valueOf(color)
                tvTooltipLabel.text = entry.label
                tvTooltipValue.text = context.getString(
                    R.string.profile_stats_percent,
                    entry.value.roundToInt()
                )

                pieChart.centerText = ""

                cardChartTooltip.animate().cancel()
                if (!cardChartTooltip.isVisible) {
                    cardChartTooltip.alpha = 0f
                    cardChartTooltip.scaleX = 0.85f
                    cardChartTooltip.scaleY = 0.85f
                    cardChartTooltip.isVisible = true
                }
                cardChartTooltip.animate()
                    .alpha(1f)
                    .scaleX(1f)
                    .scaleY(1f)
                    .setDuration(180)
                    .start()
            }
        }

        private fun hideTooltip(animate: Boolean) {
            val card = binding.cardChartTooltip
            binding.pieChart.centerText = chartCenterText
            card.animate().cancel()
            if (!animate) {
                card.isVisible = false
                card.alpha = 1f
                card.scaleX = 1f
                card.scaleY = 1f
                return
            }
            if (!card.isVisible) return
            card.animate()
                .alpha(0f)
                .scaleX(0.85f)
                .scaleY(0.85f)
                .setDuration(150)
                .withEndAction { card.isVisible = false }
                .start()
        }
    }

    data class ProfileStatsData(
        val title: String,
        var categoriesDataSet: PieDataSet? = null,
        var amountConsumedData: UserStatsData.AmountConsumedData? = null,
        var isLoading: Boolean = true
    )

}