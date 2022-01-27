package io.github.drumber.kitsune.ui.adapter.paging

import androidx.recyclerview.widget.DiffUtil
import io.github.drumber.kitsune.GlideRequests
import io.github.drumber.kitsune.data.model.media.Manga
import io.github.drumber.kitsune.ui.adapter.OnItemClickListener

class MangaAdapter(glide: GlideRequests, listener: OnItemClickListener<Manga>? = null) :
    MediaPagingAdapter<Manga>(MangaComparator, glide, listener) {

    object MangaComparator: DiffUtil.ItemCallback<Manga>() {
        override fun areItemsTheSame(oldItem: Manga, newItem: Manga) = oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: Manga, newItem: Manga) = oldItem == newItem
    }

}