package io.github.drumber.kitsune.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.appcompat.widget.TooltipCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import coil3.load
import io.github.drumber.kitsune.constants.StreamingLogo
import io.github.drumber.kitsune.data.presentation.model.media.streamer.StreamingLink
import io.github.drumber.kitsune.databinding.ItemStreamerBinding

class StreamingLinkAdapter(
    private val listener: OnItemClickListener<StreamingLink>? = null
) : ListAdapter<StreamingLink, StreamingLinkAdapter.StreamingLinkViewHolder>(DIFF_CALLBACK) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StreamingLinkViewHolder {
        val binding = ItemStreamerBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return StreamingLinkViewHolder(binding) { position ->
            if (position < itemCount) {
                listener?.onItemClick(binding.root, getItem(position))
            }
        }
    }

    override fun onBindViewHolder(holder: StreamingLinkViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class StreamingLinkViewHolder(
        private val binding: ItemStreamerBinding,
        private val listener: (Int) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        init {
            binding.root.setOnClickListener {
                val position = bindingAdapterPosition
                if(position != RecyclerView.NO_POSITION) {
                    listener(position)
                }
            }
        }

        fun bind(streamingLink: StreamingLink) {
            val logo = streamingLink.streamer?.siteName?.let { siteName ->
                StreamingLogo.entries.find { it.name.equals(siteName, true) }?.drawable
            }
            binding.ivLogo.load(logo)

            TooltipCompat.setTooltipText(binding.root, streamingLink.streamer?.siteName)
        }

    }

    companion object {
        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<StreamingLink>() {
            override fun areItemsTheSame(oldItem: StreamingLink, newItem: StreamingLink): Boolean =
                oldItem.id == newItem.id

            override fun areContentsTheSame(oldItem: StreamingLink, newItem: StreamingLink): Boolean =
                oldItem == newItem
        }
    }

}