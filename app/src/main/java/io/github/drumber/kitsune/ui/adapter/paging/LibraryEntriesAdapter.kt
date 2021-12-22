package io.github.drumber.kitsune.ui.adapter.paging

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import io.github.drumber.kitsune.GlideRequests
import io.github.drumber.kitsune.R
import io.github.drumber.kitsune.data.model.library.LibraryEntryAdapter
import io.github.drumber.kitsune.data.model.library.LibraryEntryWrapper
import io.github.drumber.kitsune.data.model.media.MediaAdapter
import io.github.drumber.kitsune.databinding.ItemLibraryEntryBinding

class LibraryEntriesAdapter(
    private val glide: GlideRequests,
    private val listener: LibraryEntryActionListener? = null
) : PagingDataAdapter<LibraryEntryWrapper, LibraryEntriesAdapter.LibraryEntryViewHolder>(
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

        fun bind(entryWrapper: LibraryEntryWrapper) {
            val entry = entryWrapper.libraryEntry
            val mediaAdapter = (entry.anime ?: entry.manga)?.let { MediaAdapter.fromMedia(it) }
            binding.apply {
                this.entry = LibraryEntryAdapter(entryWrapper)
                data = mediaAdapter
            }

            glide.load(mediaAdapter?.posterImage)
                .centerCrop()
                .placeholder(R.drawable.ic_insert_photo_48)
                .into(binding.ivThumbnail)

            binding.tvNotSynced.isVisible = entryWrapper.offlineLibraryUpdate?.let { offlineEntry ->
                 !offlineEntry.isEqualToLibraryEntry(entry)
            } ?: false
        }
    }

    object LibraryEntryComparator : DiffUtil.ItemCallback<LibraryEntryWrapper>() {
        override fun areItemsTheSame(oldItem: LibraryEntryWrapper, newItem: LibraryEntryWrapper) =
            oldItem.libraryEntry.id == newItem.libraryEntry.id

        override fun areContentsTheSame(oldItem: LibraryEntryWrapper, newItem: LibraryEntryWrapper) =
            oldItem == newItem
    }

    interface LibraryEntryActionListener {
        fun onItemClicked(item: LibraryEntryWrapper)
        fun onEpisodeWatchedClicked(item: LibraryEntryWrapper)
        fun onEpisodeUnwatchedClicked(item: LibraryEntryWrapper)
        fun onRatingClicked(item: LibraryEntryWrapper)
    }

}