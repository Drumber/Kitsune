package io.github.drumber.kitsune.ui.adapter

import android.view.View
import com.bumptech.glide.RequestManager
import io.github.drumber.kitsune.constants.MediaItemSize
import io.github.drumber.kitsune.data.model.media.relationship.MediaRelationship
import io.github.drumber.kitsune.databinding.ItemMediaBinding
import java.util.concurrent.CopyOnWriteArrayList

class MediaRelationshipRecyclerViewAdapter(
    dataSet: CopyOnWriteArrayList<MediaRelationship>,
    glide: RequestManager,
    transitionNameSuffix: String? = null,
    listener: OnItemClickListener<MediaRelationship>? = null
) : AbstractMediaRecyclerViewAdapter<MediaRelationshipViewHolder, MediaRelationship>(
    dataSet,
    glide,
    transitionNameSuffix,
    listener
) {

    override fun onCreateViewHolder(
        binding: ItemMediaBinding,
        glide: RequestManager,
        listener: (View, Int) -> Unit
    ): MediaRelationshipViewHolder {
        binding.cardMedia.setCustomItemSize(MediaItemSize.SMALL)
        return MediaRelationshipViewHolder(
            binding,
            glide,
            listener
        )
    }
}