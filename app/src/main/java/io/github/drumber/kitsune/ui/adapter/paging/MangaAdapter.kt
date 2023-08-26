package io.github.drumber.kitsune.ui.adapter.paging

import androidx.recyclerview.widget.DiffUtil
import com.bumptech.glide.RequestManager
import io.github.drumber.kitsune.domain.model.media.Manga
import io.github.drumber.kitsune.ui.adapter.OnItemClickListener

class MangaAdapter(glide: RequestManager, listener: OnItemClickListener<Manga>? = null) :
    MediaPagingAdapter<Manga>(MangaComparator, glide, listener) {

    object MangaComparator: DiffUtil.ItemCallback<Manga>() {
        override fun areItemsTheSame(oldItem: Manga, newItem: Manga) = oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: Manga, newItem: Manga) = oldItem == newItem
    }

}