package io.github.drumber.kitsune.ui.adapter

import androidx.paging.PagingData
import androidx.recyclerview.widget.DiffUtil
import io.github.drumber.kitsune.GlideRequests
import io.github.drumber.kitsune.data.model.resource.Anime
import io.github.drumber.kitsune.data.model.resource.Resource

class AnimeAdapter(glide: GlideRequests, listener: OnItemClickListener<Anime>? = null) :
    ResourcePagingAdapter<Anime>(AnimeComparator, glide, listener) {

    object AnimeComparator: DiffUtil.ItemCallback<Anime>() {
        override fun areItemsTheSame(oldItem: Anime, newItem: Anime) = oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: Anime, newItem: Anime) = oldItem == newItem
    }

}