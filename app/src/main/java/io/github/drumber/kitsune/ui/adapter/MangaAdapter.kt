package io.github.drumber.kitsune.ui.adapter

import androidx.recyclerview.widget.DiffUtil
import io.github.drumber.kitsune.GlideRequests
import io.github.drumber.kitsune.data.model.resource.Manga

class MangaAdapter(glide: GlideRequests, listener: OnItemClickListener<Manga>? = null) :
    ResourcePagingAdapter<Manga>(MangaComparator, glide, listener) {

    object MangaComparator: DiffUtil.ItemCallback<Manga>() {
        override fun areItemsTheSame(oldItem: Manga, newItem: Manga) = oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: Manga, newItem: Manga) = oldItem == newItem
    }

}