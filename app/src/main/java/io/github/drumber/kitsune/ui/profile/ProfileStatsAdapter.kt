package io.github.drumber.kitsune.ui.profile

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.text.HtmlCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import io.github.drumber.kitsune.R
import io.github.drumber.kitsune.domain_old.model.infrastructure.user.stats.StatsData
import io.github.drumber.kitsune.databinding.ItemProfileStatsBinding
import io.github.drumber.kitsune.ui.widget.chart.PieChartStyle.applyStyle
import io.github.drumber.kitsune.util.TimeUtil
import kotlin.math.roundToInt

class ProfileStatsAdapter(dataSet: List<ProfileStatsData>) :
    RecyclerView.Adapter<ProfileStatsAdapter.ProfileStatsViewHolder>() {

    companion object {
        const val POS_ANIME = 0
        const val POS_MANGA = 1
    }

    private val dataSet = dataSet.toMutableList()

    fun getDataSet() = dataSet.toList()

    fun updateData(position: Int, newData: ProfileStatsData) {
        dataSet[position] = newData
        notifyItemChanged(position)
    }

    fun updateCategoryData(position: Int, data: PieDataSet) {
        dataSet[position].categoriesDataSet = data
        notifyItemChanged(position)
    }

    fun updateAmountConsumedData(position: Int, data: StatsData.AmountConsumedData?) {
        dataSet[position].amountConsumedData = data
        notifyItemChanged(position)
    }

    fun setLoading(position: Int, isLoading: Boolean) {
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
                            percentiles.times(100).roundToInt()
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

            dataModel.categoriesDataSet?.let { set ->
                set.applyStyle(context)

                val pieData = PieData(set)
                pieData.applyStyle(context)

                binding.pieChart.apply {
                    data = pieData
                    centerText = dataModel.title
                    invalidate()
                }
            } ?: binding.pieChart.clear()
        }
    }

    data class ProfileStatsData(
        val title: String,
        var categoriesDataSet: PieDataSet? = null,
        var amountConsumedData: StatsData.AmountConsumedData? = null,
        var isLoading: Boolean = true
    )

}