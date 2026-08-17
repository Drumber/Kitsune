package io.github.drumber.kitsune.ui.adapter

import android.view.View
import coil3.load
import io.github.drumber.kitsune.data.presentation.model.media.relationship.MediaRelationship
import io.github.drumber.kitsune.data.presentation.model.media.relationship.getStringRes
import io.github.drumber.kitsune.databinding.ItemMediaBinding
import io.github.drumber.kitsune.ui.adapter.AbstractMediaRecyclerViewAdapter.AbstractMediaViewHolder

class MediaRelationshipViewHolder(
    private val binding: ItemMediaBinding,
    onClick: (View, Int) -> Unit
) : AbstractMediaViewHolder<MediaRelationship>(binding, onClick) {

    override fun bind(data: MediaRelationship) {
        binding.data = data.media
        binding.overlayTagText = data.role?.getStringRes()
            ?.let { binding.root.context.getString(it) }
        data.media?.posterImageUrl?.let {
            binding.ivThumbnail.load(it)
        }
    }
}