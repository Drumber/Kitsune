package io.github.drumber.kitsune.ui.adapter.paging

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import io.github.drumber.kitsune.GlideRequests
import io.github.drumber.kitsune.data.model.media.BaseMedia
import io.github.drumber.kitsune.data.model.media.MediaAdapter
import io.github.drumber.kitsune.databinding.ItemMediaBinding
import io.github.drumber.kitsune.ui.adapter.MediaViewHolder
import io.github.drumber.kitsune.ui.adapter.OnItemClickListener

sealed class MediaPagingAdapter<T : BaseMedia>(
    diffCallback: DiffUtil.ItemCallback<T>,
    private val glide: GlideRequests,
    private val listener: OnItemClickListener<T>? = null
) : PagingDataAdapter<T, MediaViewHolder>(diffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MediaViewHolder {
        val binding = ItemMediaBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        binding.cardMedia.isInGridLayout = true

        return MediaViewHolder(
            binding,
            glide
        ) { position ->
            getItem(position)?.let { item -> listener?.onItemClick(binding.cardMedia, item) }
        }
    }

    override fun onBindViewHolder(holder: MediaViewHolder, position: Int) {
        getItem(position)?.let { holder.bind(MediaAdapter.fromMedia(it)) }
    }

}