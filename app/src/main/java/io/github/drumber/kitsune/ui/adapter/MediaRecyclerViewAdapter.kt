package io.github.drumber.kitsune.ui.adapter

import android.view.View
import io.github.drumber.kitsune.preference.MediaItemSize
import io.github.drumber.kitsune.data.presentation.model.media.Media
import io.github.drumber.kitsune.databinding.ItemMediaBinding
import java.util.concurrent.CopyOnWriteArrayList

class MediaRecyclerViewAdapter(
    dataSet: CopyOnWriteArrayList<Media>,
    private val showSubtype: Boolean = false,
    private val itemSize: MediaItemSize? = null,
    transitionNameSuffix: String? = null,
    listener: OnItemClickListener<Media>? = null
) : AbstractMediaRecyclerViewAdapter<MediaViewHolder, Media>(
    dataSet,
    transitionNameSuffix,
    listener
) {

    override fun onCreateViewHolder(
        binding: ItemMediaBinding,
        listener: (View, Int) -> Unit
    ): MediaViewHolder {
        itemSize?.let { binding.cardMedia.setCustomItemSize(it) }

        return MediaViewHolder(
            binding,
            showSubtype,
            listener
        )
    }
}