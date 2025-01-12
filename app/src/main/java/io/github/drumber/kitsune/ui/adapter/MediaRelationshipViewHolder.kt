package io.github.drumber.kitsune.ui.adapter

import android.view.View
import com.bumptech.glide.RequestManager
import io.github.drumber.kitsune.R
import io.github.drumber.kitsune.data.presentation.model.media.relationship.MediaRelationship
import io.github.drumber.kitsune.data.presentation.extension.getStringRes
import io.github.drumber.kitsune.databinding.ItemMediaBinding
import io.github.drumber.kitsune.ui.adapter.AbstractMediaRecyclerViewAdapter.AbstractMediaViewHolder

class MediaRelationshipViewHolder(
    private val binding: ItemMediaBinding,
    private val glide: RequestManager,
    onClick: (View, Int) -> Unit
) : AbstractMediaViewHolder<MediaRelationship>(binding, onClick) {

    override fun bind(data: MediaRelationship) {
        binding.data = data.media
        binding.overlayTagText = data.role?.getStringRes()
            ?.let { binding.root.context.getString(it) }
        data.media?.posterImageUrl?.let {
            glide.load(it)
                .placeholder(R.drawable.ic_insert_photo_48)
                .into(binding.ivThumbnail)
        }
    }
}