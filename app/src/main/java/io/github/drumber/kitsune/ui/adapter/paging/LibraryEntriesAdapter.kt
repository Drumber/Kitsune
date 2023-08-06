package io.github.drumber.kitsune.ui.adapter.paging

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import io.github.drumber.kitsune.GlideRequests
import io.github.drumber.kitsune.R
import io.github.drumber.kitsune.data.model.library.LibraryEntryAdapter
import io.github.drumber.kitsune.data.model.library.LibraryEntryUiModel
import io.github.drumber.kitsune.data.model.library.LibraryEntryUiModel.StatusSeparatorModel
import io.github.drumber.kitsune.data.model.library.LibraryEntryWrapper
import io.github.drumber.kitsune.data.model.library.getStringResId
import io.github.drumber.kitsune.data.model.media.MediaAdapter
import io.github.drumber.kitsune.databinding.ItemLibraryEntryBinding
import io.github.drumber.kitsune.databinding.ItemLibraryStatusSeparatorBinding

class LibraryEntriesAdapter(
    private val glide: GlideRequests,
    private val listener: LibraryEntryActionListener? = null
) : PagingDataAdapter<LibraryEntryUiModel, RecyclerView.ViewHolder>(LibraryEntryUiModelComparator) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = when (viewType) {
        R.layout.item_library_entry -> LibraryEntryViewHolder(
            ItemLibraryEntryBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        )
        else -> StatusSeparatorViewHolder(
            ItemLibraryStatusSeparatorBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun getItemViewType(position: Int): Int {
        return when (peek(position)) {
            is LibraryEntryWrapper -> R.layout.item_library_entry
            is StatusSeparatorModel -> R.layout.item_library_status_separator
            else -> 0
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = getItem(position) ?: return
        when (holder) {
            is LibraryEntryViewHolder -> holder.bind(item as LibraryEntryWrapper)
            is StatusSeparatorViewHolder -> holder.bind(item as StatusSeparatorModel)
        }
    }

    inner class LibraryEntryViewHolder(
        private val binding: ItemLibraryEntryBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        init {
            binding.apply {
                cardView.setOnClickListener {
                    if (bindingAdapterPosition != RecyclerView.NO_POSITION) {
                        getItem(bindingAdapterPosition)?.let { listener?.onItemClicked(cardView, it as LibraryEntryWrapper) }
                    }
                }
                cardView.setOnLongClickListener {
                    if (bindingAdapterPosition != RecyclerView.NO_POSITION) {
                        getItem(bindingAdapterPosition)?.let { listener?.onItemLongClicked(it as LibraryEntryWrapper) }
                        return@setOnLongClickListener true
                    }
                    return@setOnLongClickListener false
                }
                btnWatchedAdd.setOnClickListener {
                    if (bindingAdapterPosition != RecyclerView.NO_POSITION) {
                        getItem(bindingAdapterPosition)?.let { listener?.onEpisodeWatchedClicked(it as LibraryEntryWrapper) }
                    }
                }
                btnWatchedRemoved.setOnClickListener {
                    if (bindingAdapterPosition != RecyclerView.NO_POSITION) {
                        getItem(bindingAdapterPosition)?.let {
                            listener?.onEpisodeUnwatchedClicked(it as LibraryEntryWrapper)
                        }
                    }
                }
                btnRating.setOnClickListener {
                    if (bindingAdapterPosition != RecyclerView.NO_POSITION) {
                        getItem(bindingAdapterPosition)?.let {
                            listener?.onRatingClicked(it as LibraryEntryWrapper)
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

            val isNotSynced = entryWrapper.libraryModification?.let { libraryModification ->
                !libraryModification.isEqualToLibraryEntry(entry)
            } ?: false
            binding.tvNotSynced.isVisible = isNotSynced
            binding.tvTitle.maxLines = if (isNotSynced) 2 else 3
        }
    }

    inner class StatusSeparatorViewHolder(
        private val binding: ItemLibraryStatusSeparatorBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(statusSeparator: StatusSeparatorModel) {
            binding.tvTitle.setText(statusSeparator.status.getStringResId())
        }

    }

    object LibraryEntryUiModelComparator : DiffUtil.ItemCallback<LibraryEntryUiModel>() {
        override fun areItemsTheSame(
            oldItem: LibraryEntryUiModel,
            newItem: LibraryEntryUiModel
        ): Boolean {
            val isSameLibraryEntry = oldItem is LibraryEntryWrapper
                    && newItem is LibraryEntryWrapper
                    && oldItem.libraryEntry.id == newItem.libraryEntry.id

            val isSameSeparator = oldItem is StatusSeparatorModel
                    && newItem is StatusSeparatorModel
                    && oldItem.status == newItem.status

            return isSameLibraryEntry || isSameSeparator
        }


        override fun areContentsTheSame(
            oldItem: LibraryEntryUiModel,
            newItem: LibraryEntryUiModel
        ) =
            oldItem == newItem
    }

    interface LibraryEntryActionListener {
        fun onItemClicked(view: View, item: LibraryEntryWrapper)
        fun onItemLongClicked(item: LibraryEntryWrapper)
        fun onEpisodeWatchedClicked(item: LibraryEntryWrapper)
        fun onEpisodeUnwatchedClicked(item: LibraryEntryWrapper)
        fun onRatingClicked(item: LibraryEntryWrapper)
    }

}