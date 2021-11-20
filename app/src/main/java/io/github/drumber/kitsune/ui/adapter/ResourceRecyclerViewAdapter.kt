package io.github.drumber.kitsune.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.recyclerview.widget.RecyclerView
import io.github.drumber.kitsune.GlideRequests
import io.github.drumber.kitsune.data.model.resource.ResourceAdapter
import io.github.drumber.kitsune.databinding.ItemResourceBinding
import java.util.concurrent.CopyOnWriteArrayList

class ResourceRecyclerViewAdapter(
    val dataSet: CopyOnWriteArrayList<ResourceAdapter>,
    private val glide: GlideRequests,
    private val listener: OnItemClickListener<ResourceAdapter>? = null
) : RecyclerView.Adapter<ResourceViewHolder>() {

    var overrideWidth: Int? = null
    var overrideHeight: Int? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ResourceViewHolder {
        val binding = ItemResourceBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        binding.apply {
            contentWrapper.layoutParams.width = FrameLayout.LayoutParams.WRAP_CONTENT
            cardResource.layoutParams.apply {
                overrideWidth?.let { width = it }
                overrideHeight?.let { height = it }
            }
        }
        return ResourceViewHolder(
            binding,
            glide
        ) { position ->
            if (position < dataSet.size) {
                listener?.onItemClick(dataSet[position])
            }
        }
    }

    override fun onBindViewHolder(holder: ResourceViewHolder, position: Int) {
        holder.bind(dataSet[position])
    }

    override fun getItemCount() = dataSet.size

}