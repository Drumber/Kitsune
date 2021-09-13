package io.github.drumber.kitsune.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import io.github.drumber.kitsune.GlideRequests
import io.github.drumber.kitsune.R
import io.github.drumber.kitsune.data.model.resource.Anime
import io.github.drumber.kitsune.databinding.ItemAnimeBinding
import io.github.drumber.kitsune.util.smallOrHigher

class AnimeAdapter(private val glide: GlideRequests) :
    PagingDataAdapter<Anime, AnimeAdapter.AnimeViewHolder>(AnimeComparator) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AnimeViewHolder {
        return AnimeViewHolder(
            ItemAnimeBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        )
    }

    override fun onBindViewHolder(holder: AnimeViewHolder, position: Int) {
        getItem(position)?.let { holder.bind(it) }
    }

    inner class AnimeViewHolder(private val binding: ItemAnimeBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(anime: Anime) {
            binding.anime = anime
            glide.load(anime.posterImage?.smallOrHigher())
                .centerCrop()
                .placeholder(R.drawable.ic_insert_photo_48)
                .into(binding.ivThumbnail)
        }

    }

    object AnimeComparator: DiffUtil.ItemCallback<Anime>() {
        override fun areItemsTheSame(oldItem: Anime, newItem: Anime) = oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: Anime, newItem: Anime) = oldItem == newItem
    }

}