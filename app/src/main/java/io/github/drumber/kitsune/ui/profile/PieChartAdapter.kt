package io.github.drumber.kitsune.ui.profile

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import io.github.drumber.kitsune.databinding.ItemPieChartBinding
import io.github.drumber.kitsune.ui.widget.PieChartStyle.applyStyle

class PieChartAdapter(private val dataSet: List<PieChartData>) :
    RecyclerView.Adapter<PieChartAdapter.PieChartViewHolder>() {

    fun updateData(position: Int, newDataSet: PieDataSet) {
        dataSet[position].dataSet = newDataSet
        notifyItemChanged(position)
    }

    fun setLoading(position: Int, isLoading: Boolean) {
        dataSet[position].isLoading = isLoading
        notifyItemChanged(position)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PieChartViewHolder {
        val binding = ItemPieChartBinding
            .inflate(LayoutInflater.from(parent.context), parent, false)
        return PieChartViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PieChartViewHolder, position: Int) {
        holder.bind(dataSet[position])
    }

    override fun getItemCount() = dataSet.size

    inner class PieChartViewHolder(private val binding: ItemPieChartBinding) :
        RecyclerView.ViewHolder(binding.root) {

        init {
            binding.apply {
                pieChart.applyStyle(binding.root.context)
                progressBar.isVisible = true
            }
        }

        fun bind(dataModel: PieChartData) {
            val context = binding.root.context

            val isDataEmpty = dataModel.dataSet.let { it == null || it.values.isEmpty() }
            binding.apply {
                progressBar.isVisible = dataModel.isLoading
                pieChart.isVisible = !isDataEmpty && !dataModel.isLoading
                tvNoData.isVisible = isDataEmpty && !dataModel.isLoading
            }

            dataModel.dataSet?.let { set ->
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

    data class PieChartData(
        val title: String,
        var dataSet: PieDataSet? = null,
        var isLoading: Boolean = true
    )

}