package io.github.drumber.kitsune.ui.adapter

import android.text.format.DateUtils
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.RequestManager
import io.github.drumber.kitsune.R
import io.github.drumber.kitsune.data.presentation.model.reaction.MediaReaction
import io.github.drumber.kitsune.databinding.ItemMediaReactionPreviewBinding
import io.github.drumber.kitsune.util.parseUtcDate

class MediaReactionPreviewAdapter(
    private val glide: RequestManager,
    private val onUpvoteClick: ((MediaReaction) -> Unit)? = null
) : ListAdapter<MediaReaction, MediaReactionPreviewAdapter.ReactionViewHolder>(ReactionComparator) {

    private val upvotedIds = mutableSetOf<String>()
    private val countOverrides = mutableMapOf<String, Int>()

    /** Marks the reaction as up-voted, overrides its displayed count and refreshes the item. */
    fun markUpvoted(reactionId: String, newCount: Int) {
        upvotedIds.add(reactionId)
        countOverrides[reactionId] = newCount
        val index = currentList.indexOfFirst { it.id == reactionId }
        if (index != -1) notifyItemChanged(index)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReactionViewHolder {
        return ReactionViewHolder(
            ItemMediaReactionPreviewBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        )
    }

    override fun onBindViewHolder(holder: ReactionViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ReactionViewHolder(private val binding: ItemMediaReactionPreviewBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(reaction: MediaReaction) {
            glide.load(reaction.authorAvatarUrl)
                .placeholder(R.drawable.ic_outline_person_24)
                .circleCrop()
                .into(binding.ivAvatar)

            binding.tvAuthor.text = reaction.authorName
                ?: binding.root.context.getString(R.string.feed_unknown_user)

            binding.tvTimestamp.apply {
                val date = reaction.createdAt?.parseUtcDate()
                isVisible = date != null
                text = date?.let {
                    DateUtils.getRelativeTimeSpanString(
                        it.time,
                        System.currentTimeMillis(),
                        DateUtils.MINUTE_IN_MILLIS
                    )
                }
            }

            binding.tvReaction.text = reaction.reaction?.takeUnless { it.isBlank() }
                ?: reaction.content

            val isUpvoted = upvotedIds.contains(reaction.id)
            val count = countOverrides[reaction.id] ?: reaction.upVotesCount

            binding.btnUpvote.apply {
                text = count.toString()
                setIconResource(
                    if (isUpvoted) R.drawable.ic_thumb_up_24 else R.drawable.ic_thumb_up_border_24
                )
                isEnabled = !isUpvoted
                setOnClickListener { onUpvoteClick?.invoke(reaction) }
            }
        }

    }

    object ReactionComparator : DiffUtil.ItemCallback<MediaReaction>() {
        override fun areItemsTheSame(oldItem: MediaReaction, newItem: MediaReaction) =
            oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: MediaReaction, newItem: MediaReaction) =
            oldItem == newItem
    }

}
