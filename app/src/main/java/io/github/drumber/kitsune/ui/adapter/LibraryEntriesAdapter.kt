package io.github.drumber.kitsune.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import io.github.drumber.kitsune.GlideRequests
import io.github.drumber.kitsune.R
import io.github.drumber.kitsune.data.model.library.LibraryEntry
import io.github.drumber.kitsune.data.model.library.LibraryEntryAdapter
import io.github.drumber.kitsune.data.model.resource.ResourceAdapter
import io.github.drumber.kitsune.databinding.ItemLibraryEntryBinding

class LibraryEntriesAdapter(
    private val glide: GlideRequests,
    private val listener: LibraryEntryActionListener? = null
) : PagingDataAdapter<LibraryEntry, LibraryEntriesAdapter.LibraryEntryViewHolder>(
    LibraryEntryComparator
) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LibraryEntryViewHolder {
        return LibraryEntryViewHolder(
            ItemLibraryEntryBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        )
    }

    override fun onBindViewHolder(holder: LibraryEntryViewHolder, position: Int) {
        getItem(position)?.let { holder.bind(it) }
    }

    inner class LibraryEntryViewHolder(
        private val binding: ItemLibraryEntryBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        init {
            binding.apply {
                cardView.setOnClickListener {
                    if (bindingAdapterPosition != RecyclerView.NO_POSITION) {
                        getItem(bindingAdapterPosition)?.let { listener?.onItemClicked(it) }
                    }
                }
                btnWatchedAdd.setOnClickListener {
                    if (bindingAdapterPosition != RecyclerView.NO_POSITION) {
                        getItem(bindingAdapterPosition)?.let { listener?.onEpisodeWatchedClicked(it) }
                    }
                }
                btnWatchedRemoved.setOnClickListener {
                    if (bindingAdapterPosition != RecyclerView.NO_POSITION) {
                        getItem(bindingAdapterPosition)?.let {
                            listener?.onEpisodeUnwatchedClicked(it)
                        }
                    }
                }
                btnRating.setOnClickListener {
                    if (bindingAdapterPosition != RecyclerView.NO_POSITION) {
                        getItem(bindingAdapterPosition)?.let {
                            listener?.onRatingClicked(it)
                        }
                    }
                }
            }
        }

        fun bind(entry: LibraryEntry) {
            val resourceAdapter = (entry.anime ?: entry.manga)?.let { ResourceAdapter.fromResource(it) }
            binding.apply {
                this.entry = LibraryEntryAdapter(entry)
                data = resourceAdapter
            }
            glide.load(resourceAdapter?.posterImage)
                .centerCrop()
                .placeholder(R.drawable.ic_insert_photo_48)
                .into(binding.ivThumbnail)
        }
    }

    object LibraryEntryComparator : DiffUtil.ItemCallback<LibraryEntry>() {
        override fun areItemsTheSame(oldItem: LibraryEntry, newItem: LibraryEntry) =
            oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: LibraryEntry, newItem: LibraryEntry) =
            oldItem == newItem
    }

    interface LibraryEntryActionListener {
        fun onItemClicked(item: LibraryEntry)
        fun onEpisodeWatchedClicked(item: LibraryEntry)
        fun onEpisodeUnwatchedClicked(item: LibraryEntry)
        fun onRatingClicked(item: LibraryEntry)
    }

}