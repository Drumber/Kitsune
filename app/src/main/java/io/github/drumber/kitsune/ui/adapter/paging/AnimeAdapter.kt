package io.github.drumber.kitsune.ui.adapter.paging

import androidx.recyclerview.widget.DiffUtil
import io.github.drumber.kitsune.data.presentation.model.media.Anime
import io.github.drumber.kitsune.ui.adapter.OnItemClickListener

class AnimeAdapter(listener: OnItemClickListener<Anime>? = null) :
    MediaPagingAdapter<Anime>(AnimeComparator, listener) {

    object AnimeComparator: DiffUtil.ItemCallback<Anime>() {
        override fun areItemsTheSame(oldItem: Anime, newItem: Anime) = oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: Anime, newItem: Anime) = oldItem == newItem
    }

}