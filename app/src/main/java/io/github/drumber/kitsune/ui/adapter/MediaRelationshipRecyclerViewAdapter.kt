package io.github.drumber.kitsune.ui.adapter

import android.view.View
import io.github.drumber.kitsune.preference.MediaItemSize
import io.github.drumber.kitsune.data.presentation.model.media.relationship.MediaRelationship
import io.github.drumber.kitsune.databinding.ItemMediaBinding
import java.util.concurrent.CopyOnWriteArrayList

class MediaRelationshipRecyclerViewAdapter(
    dataSet: CopyOnWriteArrayList<MediaRelationship>,
    transitionNameSuffix: String? = null,
    listener: OnItemClickListener<MediaRelationship>? = null
) : AbstractMediaRecyclerViewAdapter<MediaRelationshipViewHolder, MediaRelationship>(
    dataSet,
    transitionNameSuffix,
    listener
) {

    override fun onCreateViewHolder(
        binding: ItemMediaBinding,
        listener: (View, Int) -> Unit
    ): MediaRelationshipViewHolder {
        binding.cardMedia.setCustomItemSize(MediaItemSize.SMALL)
        return MediaRelationshipViewHolder(
            binding,
            listener
        )
    }
}