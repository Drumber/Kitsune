package io.github.drumber.kitsune.ui.adapter

import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.RequestManager
import io.github.drumber.kitsune.R
import io.github.drumber.kitsune.data.model.media.MediaAdapter
import io.github.drumber.kitsune.databinding.ItemMediaBinding

class MediaViewHolder(
    private val binding: ItemMediaBinding,
    private val glide: RequestManager,
    private val tagData: TagData = TagData.None,
    private val listener: (position: Int) -> Unit
): RecyclerView.ViewHolder(binding.root) {

    /**
     * Information type for the overlay tag.
     */
    enum class TagData {
        None,
        Subtype,
        RelationshipRole
    }

    init {
        binding.cardMedia.setOnClickListener {
            val position = bindingAdapterPosition
            if(position != RecyclerView.NO_POSITION) {
                listener(position)
            }
        }
    }

    fun bind(data: MediaAdapter) {
        binding.data = data
        binding.overlayTagText = when (tagData) {
            TagData.None -> null
            TagData.Subtype -> data.subtype
            TagData.RelationshipRole -> data.ownRelationshipRoleText(binding.root.context)
        }
        glide.load(data.posterImage)
            .placeholder(R.drawable.ic_insert_photo_48)
            .into(binding.ivThumbnail)
    }

}