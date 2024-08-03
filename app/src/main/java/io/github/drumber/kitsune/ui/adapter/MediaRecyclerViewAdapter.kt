package io.github.drumber.kitsune.ui.adapter

import android.view.View
import com.bumptech.glide.RequestManager
import io.github.drumber.kitsune.constants.MediaItemSize
import io.github.drumber.kitsune.data.presentation.model.media.Media
import io.github.drumber.kitsune.databinding.ItemMediaBinding
import java.util.concurrent.CopyOnWriteArrayList

class MediaRecyclerViewAdapter(
    dataSet: CopyOnWriteArrayList<Media>,
    glide: RequestManager,
    private val showSubtype: Boolean = false,
    private val itemSize: MediaItemSize? = null,
    transitionNameSuffix: String? = null,
    listener: OnItemClickListener<Media>? = null
) : AbstractMediaRecyclerViewAdapter<MediaViewHolder, Media>(
    dataSet,
    glide,
    transitionNameSuffix,
    listener
) {

    var overrideItemSize: MediaItemSize? = null

    override fun onCreateViewHolder(
        binding: ItemMediaBinding,
        glide: RequestManager,
        listener: (View, Int) -> Unit
    ): MediaViewHolder {
        itemSize?.let { binding.cardMedia.setCustomItemSize(it) }

        return MediaViewHolder(
            binding,
            glide,
            showSubtype,
            listener
        )
    }
}