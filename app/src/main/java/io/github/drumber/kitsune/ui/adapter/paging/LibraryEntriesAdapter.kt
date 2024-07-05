package io.github.drumber.kitsune.ui.adapter.paging

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.RequestManager
import io.github.drumber.kitsune.R
import io.github.drumber.kitsune.data.presentation.model.library.LibraryEntryWithModification
import io.github.drumber.kitsune.data.presentation.model.library.getStringResId
import io.github.drumber.kitsune.databinding.ItemLibraryEntryBinding
import io.github.drumber.kitsune.databinding.ItemLibraryStatusSeparatorBinding
import io.github.drumber.kitsune.data.presentation.model.library.LibraryEntryUiModel
import io.github.drumber.kitsune.data.presentation.model.library.LibraryEntryUiModel.EntryModel
import io.github.drumber.kitsune.data.presentation.model.library.LibraryEntryUiModel.StatusSeparatorModel

class LibraryEntriesAdapter(
    private val glide: RequestManager,
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
            is EntryModel -> R.layout.item_library_entry
            is StatusSeparatorModel -> R.layout.item_library_status_separator
            else -> 0
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = getItem(position) ?: return
        when (holder) {
            is LibraryEntryViewHolder -> holder.bind((item as EntryModel).entry)
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
                        getItem(bindingAdapterPosition)?.let { listener?.onItemClicked(cardView, (it as EntryModel).entry) }
                    }
                }
                cardView.setOnLongClickListener {
                    if (bindingAdapterPosition != RecyclerView.NO_POSITION) {
                        getItem(bindingAdapterPosition)?.let { listener?.onItemLongClicked((it as EntryModel).entry) }
                        return@setOnLongClickListener true
                    }
                    return@setOnLongClickListener false
                }
                btnWatchedAdd.setOnClickListener {
                    if (bindingAdapterPosition != RecyclerView.NO_POSITION) {
                        getItem(bindingAdapterPosition)?.let { listener?.onEpisodeWatchedClicked((it as EntryModel).entry) }
                    }
                }
                btnWatchedRemoved.setOnClickListener {
                    if (bindingAdapterPosition != RecyclerView.NO_POSITION) {
                        getItem(bindingAdapterPosition)?.let {
                            listener?.onEpisodeUnwatchedClicked((it as EntryModel).entry)
                        }
                    }
                }
                btnRating.setOnClickListener {
                    if (bindingAdapterPosition != RecyclerView.NO_POSITION) {
                        getItem(bindingAdapterPosition)?.let {
                            listener?.onRatingClicked((it as EntryModel).entry)
                        }
                    }
                }
            }
        }

        fun bind(libraryEntry: LibraryEntryWithModification) {
            binding.apply {
                this.entry = libraryEntry
            }

            glide.load(libraryEntry.media?.posterImageUrl)
                .centerCrop()
                .placeholder(R.drawable.ic_insert_photo_48)
                .into(binding.ivThumbnail)

            binding.tvNotSynced.isVisible = libraryEntry.isNotSynced
            binding.tvTitle.maxLines = if (libraryEntry.isNotSynced) 2 else 3
        }
    }

    inner class StatusSeparatorViewHolder(
        private val binding: ItemLibraryStatusSeparatorBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(statusSeparator: StatusSeparatorModel) {
            binding.tvTitle.setText(statusSeparator.status.getStringResId(!statusSeparator.isMangaSelected))
        }

    }

    object LibraryEntryUiModelComparator : DiffUtil.ItemCallback<LibraryEntryUiModel>() {
        override fun areItemsTheSame(
            oldItem: LibraryEntryUiModel,
            newItem: LibraryEntryUiModel
        ): Boolean {
            val isSameLibraryEntry = oldItem is EntryModel
                    && newItem is EntryModel
                    && oldItem.entry.id == newItem.entry.id

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
        fun onItemClicked(view: View, item: LibraryEntryWithModification)
        fun onItemLongClicked(item: LibraryEntryWithModification)
        fun onEpisodeWatchedClicked(item: LibraryEntryWithModification)
        fun onEpisodeUnwatchedClicked(item: LibraryEntryWithModification)
        fun onRatingClicked(item: LibraryEntryWithModification)
    }
}