package io.github.drumber.kitsune.ui.adapter

import android.view.View
import coil3.load
import io.github.drumber.kitsune.data.presentation.model.media.Media
import io.github.drumber.kitsune.databinding.ItemMediaBinding
import io.github.drumber.kitsune.util.fixImageUrl

class MediaViewHolder(
    private val binding: ItemMediaBinding,
    private val showSubtype: Boolean = false,
    listener: (View, Int) -> Unit
) : AbstractMediaRecyclerViewAdapter.AbstractMediaViewHolder<Media>(binding, listener) {

    override fun bind(data: Media) {
        binding.data = data
        binding.overlayTagText = when (showSubtype) {
            false -> null
            true -> data.subtypeFormatted
        }
        binding.ivThumbnail.load(data.posterImageUrl?.fixImageUrl())
    }
}
