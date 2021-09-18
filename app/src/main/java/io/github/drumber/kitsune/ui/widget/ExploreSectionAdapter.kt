package io.github.drumber.kitsune.ui.widget

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import io.github.drumber.kitsune.GlideRequests
import io.github.drumber.kitsune.data.model.resource.ResourceAdapter
import io.github.drumber.kitsune.databinding.ItemResourceBinding
import io.github.drumber.kitsune.ui.adapter.OnItemClickListener
import io.github.drumber.kitsune.ui.adapter.ResourceViewHolder
import java.util.concurrent.CopyOnWriteArrayList

class ExploreSectionAdapter(
    val dataSet: CopyOnWriteArrayList<ResourceAdapter>,
    private val glide: GlideRequests,
    private val listener: OnItemClickListener<ResourceAdapter>? = null
) : RecyclerView.Adapter<ResourceViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ResourceViewHolder {
        return ResourceViewHolder(
            ItemResourceBinding.inflate(LayoutInflater.from(parent.context), parent, false),
            glide
        ) { position ->
            if(position < dataSet.size) {
                listener?.onItemClick(dataSet[position])
            }
        }
    }

    override fun onBindViewHolder(holder: ResourceViewHolder, position: Int) {
        holder.bind(dataSet[position])
    }

    override fun getItemCount() = dataSet.size

}