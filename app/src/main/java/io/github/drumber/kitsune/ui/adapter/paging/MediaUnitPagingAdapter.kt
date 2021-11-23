package io.github.drumber.kitsune.ui.adapter.paging

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import io.github.drumber.kitsune.GlideRequests
import io.github.drumber.kitsune.R
import io.github.drumber.kitsune.data.model.unit.Chapter
import io.github.drumber.kitsune.data.model.unit.Episode
import io.github.drumber.kitsune.data.model.unit.MediaUnit
import io.github.drumber.kitsune.data.model.unit.MediaUnitAdapter
import io.github.drumber.kitsune.databinding.ItemEpisodeBinding
import io.github.drumber.kitsune.ui.adapter.OnItemClickListener
import io.github.drumber.kitsune.util.originalOrDown

class MediaUnitPagingAdapter(
    private val glide: GlideRequests,
    private val posterUrl: String?,
    private val listener: OnItemClickListener<MediaUnit>
) : PagingDataAdapter<MediaUnit, MediaUnitPagingAdapter.MediaUnitViewHolder>(MediaUnitComparator) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MediaUnitViewHolder {
        return MediaUnitViewHolder(
            ItemEpisodeBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        )
    }

    override fun onBindViewHolder(holder: MediaUnitViewHolder, position: Int) {
        getItem(position)?.let { holder.bind(it) }
    }

    inner class MediaUnitViewHolder(private val binding: ItemEpisodeBinding) :
        RecyclerView.ViewHolder(binding.root) {

        init {
            binding.root.setOnClickListener {
                if (bindingAdapterPosition != RecyclerView.NO_POSITION) {
                    getItem(bindingAdapterPosition)?.let { listener.onItemClick(it) }
                }
            }
        }

        fun bind(unit: MediaUnit) {
            glide.load(unit.thumbnail?.originalOrDown() ?: posterUrl)
                .centerCrop()
                .placeholder(R.drawable.ic_insert_photo_48)
                .into(binding.ivThumbnail)

            val adapter = MediaUnitAdapter.fromMediaUnit(unit)

            binding.apply {
                tvEpisodeTitle.text = adapter.title(root.context)
                tvEpisodeNumber.text = if (adapter.hasValidTitle) {
                    adapter.numberText(root.context)
                } else {
                    null
                }
            }
        }

    }

    object MediaUnitComparator : DiffUtil.ItemCallback<MediaUnit>() {
        override fun areItemsTheSame(oldItem: MediaUnit, newItem: MediaUnit) =
            oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: MediaUnit, newItem: MediaUnit) = when {
            oldItem is Episode && newItem is Episode -> oldItem as Episode == newItem as Episode
            oldItem is Chapter && newItem is Chapter -> oldItem as Chapter == newItem as Chapter
            else -> oldItem.id == newItem.id
        }
    }

}