package io.github.drumber.kitsune.ui.adapter.paging

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import com.bumptech.glide.RequestManager
import io.github.drumber.kitsune.data.mapper.AlgoliaMapper.toMedia
import io.github.drumber.kitsune.data.source.network.algolia.model.search.AlgoliaMediaSearchResult
import io.github.drumber.kitsune.databinding.ItemMediaBinding
import io.github.drumber.kitsune.ui.adapter.MediaViewHolder
import io.github.drumber.kitsune.ui.adapter.MediaViewHolder.TagData
import io.github.drumber.kitsune.ui.adapter.OnItemClickListener

class MediaSearchPagingAdapter(
    private val glide: RequestManager,
    private val listener: OnItemClickListener<AlgoliaMediaSearchResult>? = null
) : PagingDataAdapter<AlgoliaMediaSearchResult, MediaViewHolder>(MediaSearchComparator) {

    override fun onBindViewHolder(holder: MediaViewHolder, position: Int) {
        getItem(position)?.let { holder.bind(it.toMedia()) }
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

    object MediaSearchComparator : DiffUtil.ItemCallback<AlgoliaMediaSearchResult>() {
        override fun areItemsTheSame(oldItem: AlgoliaMediaSearchResult, newItem: AlgoliaMediaSearchResult) =
            oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: AlgoliaMediaSearchResult, newItem: AlgoliaMediaSearchResult) =
            oldItem == newItem
    }

}