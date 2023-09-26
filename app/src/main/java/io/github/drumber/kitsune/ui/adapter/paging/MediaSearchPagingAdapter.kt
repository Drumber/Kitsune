package io.github.drumber.kitsune.ui.adapter.paging

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import com.bumptech.glide.RequestManager
import io.github.drumber.kitsune.databinding.ItemMediaBinding
import io.github.drumber.kitsune.domain.mapper.toMedia
import io.github.drumber.kitsune.domain.model.infrastructure.algolia.search.MediaSearchResult
import io.github.drumber.kitsune.domain.model.ui.media.MediaAdapter
import io.github.drumber.kitsune.ui.adapter.MediaViewHolder
import io.github.drumber.kitsune.ui.adapter.MediaViewHolder.TagData
import io.github.drumber.kitsune.ui.adapter.OnItemClickListener

class MediaSearchPagingAdapter(
    private val glide: RequestManager,
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