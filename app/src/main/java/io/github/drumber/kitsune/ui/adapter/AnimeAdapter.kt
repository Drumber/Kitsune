package io.github.drumber.kitsune.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import io.github.drumber.kitsune.GlideRequests
import io.github.drumber.kitsune.R
import io.github.drumber.kitsune.data.model.resource.ResourceAdapter
import io.github.drumber.kitsune.data.model.resource.anime.Anime
import io.github.drumber.kitsune.databinding.ItemResourceBinding
import io.github.drumber.kitsune.util.smallOrHigher

class AnimeAdapter(private val glide: GlideRequests, private val listener: OnItemClickListener? = null) :
    PagingDataAdapter<Anime, AnimeAdapter.AnimeViewHolder>(AnimeComparator) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AnimeViewHolder {
        return AnimeViewHolder(
            ItemResourceBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        )
    }

    override fun onBindViewHolder(holder: AnimeViewHolder, position: Int) {
        getItem(position)?.let { holder.bind(it) }
    }

    inner class AnimeViewHolder(private val binding: ItemResourceBinding) :
        RecyclerView.ViewHolder(binding.root) {

        init {
            binding.ivThumbnail.setOnClickListener {
                val position = bindingAdapterPosition
                if(position != RecyclerView.NO_POSITION) {
                    getItem(position)?.let { item -> listener?.onItemClick(item) }
                }
            }
        }

        fun bind(anime: Anime) {
            binding.data = ResourceAdapter.AnimeResource(anime)
            glide.load(anime.posterImage?.smallOrHigher())
                .centerCrop()
                .placeholder(R.drawable.ic_insert_photo_48)
                .into(binding.ivThumbnail)
        }

    }

    fun interface OnItemClickListener {
        fun onItemClick(anime: Anime)
    }

    object AnimeComparator: DiffUtil.ItemCallback<Anime>() {
        override fun areItemsTheSame(oldItem: Anime, newItem: Anime) = oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: Anime, newItem: Anime) = oldItem == newItem
    }

}