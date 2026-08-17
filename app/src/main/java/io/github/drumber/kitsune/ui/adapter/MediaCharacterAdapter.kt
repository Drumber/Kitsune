package io.github.drumber.kitsune.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import coil3.load
import io.github.drumber.kitsune.constants.MediaItemSize
import io.github.drumber.kitsune.data.presentation.model.character.MediaCharacter
import io.github.drumber.kitsune.data.presentation.model.character.getStringRes
import io.github.drumber.kitsune.databinding.ItemMediaBinding

class MediaCharacterAdapter(
    private val listener: OnItemClickListener<MediaCharacter>? = null
) : ListAdapter<MediaCharacter, MediaCharacterAdapter.MediaCharacterViewHolder>(DIFF_CALLBACK) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MediaCharacterViewHolder {
        return MediaCharacterViewHolder(
            ItemMediaBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: MediaCharacterViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class MediaCharacterViewHolder(private val binding: ItemMediaBinding) :
        RecyclerView.ViewHolder(binding.root) {

        init {
            binding.apply {
                contentWrapper.layoutParams.width = FrameLayout.LayoutParams.WRAP_CONTENT
                cardMedia.setCustomItemSize(MediaItemSize.SMALL)
                cardMedia.isInGridLayout = false
            }
        }

        fun bind(data: MediaCharacter) {
            val media = data.media
            binding.data = media
            binding.overlayTagText = data.role?.getStringRes()?.let { binding.root.context.getString(it) }

            binding.ivThumbnail.load(media?.posterImageUrl)

            binding.cardMedia.setOnClickListener {
                listener?.onItemClick(binding.cardMedia, data)
            }
        }

    }

    companion object {
        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<MediaCharacter>() {
            override fun areItemsTheSame(oldItem: MediaCharacter, newItem: MediaCharacter): Boolean =
                oldItem.id == newItem.id

            override fun areContentsTheSame(oldItem: MediaCharacter, newItem: MediaCharacter): Boolean =
                oldItem == newItem
        }
    }

}