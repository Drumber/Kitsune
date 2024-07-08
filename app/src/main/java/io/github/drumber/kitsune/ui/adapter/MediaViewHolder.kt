package io.github.drumber.kitsune.ui.adapter

import android.view.View
import com.bumptech.glide.RequestManager
import io.github.drumber.kitsune.R
import io.github.drumber.kitsune.data.presentation.model.media.Media
import io.github.drumber.kitsune.databinding.ItemMediaBinding

class MediaViewHolder(
    private val binding: ItemMediaBinding,
    private val glide: RequestManager,
    private val showSubtype: Boolean = false,
    listener: (View, Int) -> Unit
) : AbstractMediaRecyclerViewAdapter.AbstractMediaViewHolder<Media>(binding, listener) {

    override fun bind(data: Media) {
        binding.data = data
        binding.overlayTagText = when (showSubtype) {
            false -> null
            true -> data.subtypeFormatted
        }
        glide.load(data.posterImageUrl)
            .placeholder(R.drawable.ic_insert_photo_48)
            .into(binding.ivThumbnail)
    }
}