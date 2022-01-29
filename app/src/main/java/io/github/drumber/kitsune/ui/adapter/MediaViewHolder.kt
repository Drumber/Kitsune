package io.github.drumber.kitsune.ui.adapter

import androidx.recyclerview.widget.RecyclerView
import io.github.drumber.kitsune.GlideRequests
import io.github.drumber.kitsune.R
import io.github.drumber.kitsune.data.model.media.MediaAdapter
import io.github.drumber.kitsune.databinding.ItemMediaBinding

class MediaViewHolder(
    private val binding: ItemMediaBinding,
    private val glide: GlideRequests,
    private val showSubtype: Boolean = false,
    private val listener: (position: Int) -> Unit
): RecyclerView.ViewHolder(binding.root) {

    init {
        binding.cardMedia.setOnClickListener {
            val position = bindingAdapterPosition
            if(position != RecyclerView.NO_POSITION) {
                listener(position)
            }
        }
    }

    fun bind(data: MediaAdapter) {
        binding.data = data
        binding.showSubtype = showSubtype
        glide.load(data.posterImage)
            .placeholder(R.drawable.ic_insert_photo_48)
            .into(binding.ivThumbnail)
    }

}