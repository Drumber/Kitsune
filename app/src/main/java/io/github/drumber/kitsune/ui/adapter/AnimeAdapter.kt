package io.github.drumber.kitsune.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import io.github.drumber.kitsune.GlideRequests
import io.github.drumber.kitsune.data.model.resource.ResourceAdapter
import io.github.drumber.kitsune.data.model.resource.anime.Anime
import io.github.drumber.kitsune.databinding.ItemResourceBinding

class AnimeAdapter(private val glide: GlideRequests, private val listener: OnItemClickListener<Anime>? = null) :
    PagingDataAdapter<Anime, ResourceViewHolder>(AnimeComparator) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ResourceViewHolder {
        return ResourceViewHolder(
            ItemResourceBinding.inflate(LayoutInflater.from(parent.context), parent, false),
            glide
        ) { position ->
            getItem(position)?.let { item -> listener?.onItemClick(item) }
        }
    }

    override fun onBindViewHolder(holder: ResourceViewHolder, position: Int) {
        getItem(position)?.let { holder.bind(ResourceAdapter.AnimeResource(it)) }
    }

    object AnimeComparator: DiffUtil.ItemCallback<Anime>() {
        override fun areItemsTheSame(oldItem: Anime, newItem: Anime) = oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: Anime, newItem: Anime) = oldItem == newItem
    }

}