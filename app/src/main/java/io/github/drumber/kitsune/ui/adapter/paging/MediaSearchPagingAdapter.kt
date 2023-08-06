package io.github.drumber.kitsune.ui.adapter.paging

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import io.github.drumber.kitsune.GlideRequests
import io.github.drumber.kitsune.data.model.media.MediaAdapter
import io.github.drumber.kitsune.data.model.media.MediaSearchResult
import io.github.drumber.kitsune.data.model.media.toMedia
import io.github.drumber.kitsune.databinding.ItemMediaBinding
import io.github.drumber.kitsune.ui.adapter.MediaViewHolder
import io.github.drumber.kitsune.ui.adapter.MediaViewHolder.TagData
import io.github.drumber.kitsune.ui.adapter.OnItemClickListener

class MediaSearchPagingAdapter(
    private val glide: GlideRequests,
    private val listener: OnItemClickListener<MediaSearchResult>? = null
) : PagingDataAdapter<MediaSearchResult, MediaViewHolder>(MediaSearchComparator) {

    override fun onBindViewHolder(holder: MediaViewHolder, position: Int) {
        getItem(position)?.let { holder.bind(MediaAdapter.fromMedia(it.toMedia())) }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MediaViewHolder {
        val binding = ItemMediaBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        binding.cardMedia.isInGridLayout = true

        return MediaViewHolder(
            binding,
            glide,
            TagData.Subtype
        ) { position ->
            getItem(position)?.let { item -> listener?.onItemClick(binding.cardMedia, item) }
        }
    }

    object MediaSearchComparator : DiffUtil.ItemCallback<MediaSearchResult>() {
        override fun areItemsTheSame(oldItem: MediaSearchResult, newItem: MediaSearchResult) =
            oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: MediaSearchResult, newItem: MediaSearchResult) =
            oldItem == newItem
    }

}