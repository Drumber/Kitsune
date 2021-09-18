package io.github.drumber.kitsune.ui.adapter

import androidx.recyclerview.widget.RecyclerView
import io.github.drumber.kitsune.GlideRequests
import io.github.drumber.kitsune.R
import io.github.drumber.kitsune.data.model.resource.ResourceAdapter
import io.github.drumber.kitsune.databinding.ItemResourceBinding

class ResourceViewHolder(
    private val binding: ItemResourceBinding,
    private val glide: GlideRequests,
    private val listener: (position: Int) -> Unit
): RecyclerView.ViewHolder(binding.root) {

    init {
        binding.ivThumbnail.setOnClickListener {
            val position = bindingAdapterPosition
            if(position != RecyclerView.NO_POSITION) {
                listener(position)
            }
        }
    }

    fun bind(data: ResourceAdapter) {
        binding.data = data
        glide.load(data.posterImage)
            .centerCrop()
            .placeholder(R.drawable.ic_insert_photo_48)
            .into(binding.ivThumbnail)
    }

}