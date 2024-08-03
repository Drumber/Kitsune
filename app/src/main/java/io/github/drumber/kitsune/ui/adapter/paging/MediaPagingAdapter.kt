package io.github.drumber.kitsune.ui.adapter.paging

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import com.bumptech.glide.RequestManager
import io.github.drumber.kitsune.data.presentation.model.media.Media
import io.github.drumber.kitsune.databinding.ItemMediaBinding
import io.github.drumber.kitsune.ui.adapter.MediaViewHolder
import io.github.drumber.kitsune.ui.adapter.OnItemClickListener

sealed class MediaPagingAdapter<T : Media>(
    diffCallback: DiffUtil.ItemCallback<T>,
    private val glide: RequestManager,
    private val listener: OnItemClickListener<T>? = null
) : PagingDataAdapter<T, MediaViewHolder>(diffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MediaViewHolder {
        val binding = ItemMediaBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        binding.cardMedia.isInGridLayout = true

        return MediaViewHolder(
            binding,
            glide
        ) { _, position ->
            getItem(position)?.let { item -> listener?.onItemClick(binding.cardMedia, item) }
        }
    }

    override fun onBindViewHolder(holder: MediaViewHolder, position: Int) {
        if (position >= itemCount) return
        getItem(position)?.let { holder.bind(it) }
    }

}