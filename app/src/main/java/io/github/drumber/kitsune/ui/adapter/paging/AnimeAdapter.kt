package io.github.drumber.kitsune.ui.adapter.paging

import androidx.recyclerview.widget.DiffUtil
import io.github.drumber.kitsune.GlideRequests
import io.github.drumber.kitsune.data.model.media.Anime
import io.github.drumber.kitsune.ui.adapter.OnItemClickListener

class AnimeAdapter(glide: GlideRequests, listener: OnItemClickListener<Anime>? = null) :
    MediaPagingAdapter<Anime>(AnimeComparator, glide, listener) {

    object AnimeComparator: DiffUtil.ItemCallback<Anime>() {
        override fun areItemsTheSame(oldItem: Anime, newItem: Anime) = oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: Anime, newItem: Anime) = oldItem == newItem
    }

}