package io.github.drumber.kitsune.ui.adapter.paging

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import com.bumptech.glide.RequestManager
import io.github.drumber.kitsune.data.model.media.Media
import io.github.drumber.kitsune.databinding.ItemMediaBinding
import io.github.drumber.kitsune.ui.adapter.MediaViewHolder
import io.github.drumber.kitsune.ui.adapter.OnItemClickListener

class MediaSearchPagingAdapter(
    private val glide: RequestManager,
    private val listener: OnItemClickListener<Media>? = null
) : PagingDataAdapter<Media, MediaViewHolder>(MediaSearchComparator) {

    override fun onBindViewHolder(holder: MediaViewHolder, position: Int) {
        if (position >= itemCount) return
        getItem(position)?.let { holder.bind(it) }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MediaViewHolder {
        val binding = ItemMediaBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        binding.cardMedia.isInGridLayout = true

        return MediaViewHolder(
            binding,
            glide,
            showSubtype = true
        ) { _, position ->
            getItem(position)?.let { item -> listener?.onItemClick(binding.cardMedia, item) }
        }
    }

    object MediaSearchComparator : DiffUtil.ItemCallback<Media>() {
        override fun areItemsTheSame(oldItem: Media, newItem: Media) =
            oldItem.mediaType == newItem.mediaType && oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: Media, newItem: Media) =
            oldItem == newItem
    }
}