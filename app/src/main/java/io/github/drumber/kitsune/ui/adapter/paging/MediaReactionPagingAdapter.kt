package io.github.drumber.kitsune.ui.adapter.paging

import android.text.format.DateUtils
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.appcompat.widget.PopupMenu
import androidx.core.view.isVisible
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import coil3.ImageLoader
import coil3.load
import coil3.request.error
import coil3.request.fallback
import coil3.request.placeholder
import io.github.drumber.kitsune.R
import io.github.drumber.kitsune.data.presentation.model.reaction.MediaReaction
import io.github.drumber.kitsune.databinding.ItemMediaReactionBinding
import io.github.drumber.kitsune.util.parseUtcDate

class MediaReactionPagingAdapter(
    private val imageLoader: ImageLoader,
    private val currentUserId: String?,
    private val onItemClick: (MediaReaction) -> Unit,
    private val onUpvoteClick: (MediaReaction) -> Unit,
    private val onEditClick: (MediaReaction) -> Unit,
    private val onDeleteClick: (MediaReaction) -> Unit,
    private val onShareClick: (MediaReaction) -> Unit,
    private val onAuthorClick: (String) -> Unit,
    private val onReportClick: (MediaReaction) -> Unit,
) : PagingDataAdapter<MediaReaction, MediaReactionPagingAdapter.ReactionViewHolder>(ReactionComparator) {

    private val upvotedIds = mutableSetOf<String>()
    private val countOverrides = mutableMapOf<String, Int>()

    /** Marks the reaction as up-voted, overrides its displayed count and refreshes the item. */
    fun markUpvoted(reactionId: String, newCount: Int) {
        upvotedIds.add(reactionId)
        countOverrides[reactionId] = newCount
        val index = snapshot().items.indexOfFirst { it.id == reactionId }
        if (index != -1) notifyItemChanged(index)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReactionViewHolder {
        return ReactionViewHolder(
            ItemMediaReactionBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        )
    }

    override fun onBindViewHolder(holder: ReactionViewHolder, position: Int) {
        if (position >= itemCount) return
        getItem(position)?.let { holder.bind(it) }
    }

    inner class ReactionViewHolder(private val binding: ItemMediaReactionBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(reaction: MediaReaction) {
            binding.root.setOnClickListener { onItemClick.invoke(reaction) }

            binding.ivAvatar.load(reaction.authorAvatarUrl, imageLoader = imageLoader) {
                placeholder(R.drawable.ic_outline_person_24)
                error(R.drawable.ic_outline_person_24)
                fallback(R.drawable.ic_outline_person_24)
            }

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
                setOnClickListener { onUpvoteClick.invoke(reaction) }
            }

            if (reaction.authorId != null) {
                binding.tvAuthor.setOnClickListener { onAuthorClick.invoke(reaction.authorId) }
                binding.ivAvatar.setOnClickListener { onAuthorClick.invoke(reaction.authorId) }
            } else {
                binding.tvAuthor.setOnClickListener(null)
                binding.ivAvatar.setOnClickListener(null)
            }

            bindOverflowMenu(reaction)
        }

        private fun bindOverflowMenu(reaction: MediaReaction) {
            val isOwner = currentUserId != null && reaction.authorId == currentUserId

            binding.btnOverflow.setOnClickListener { anchor ->
                PopupMenu(anchor.context, anchor).apply {
                    menuInflater.inflate(R.menu.feed_item_options_menu, menu)
                    if (!isOwner) {
                        menu.removeItem(R.id.action_edit_item)
                        menu.removeItem(R.id.action_delete_item)
                    }
                    if (isOwner || currentUserId == null) {
                        menu.removeItem(R.id.action_report_item)
                    }

                    setOnMenuItemClickListener { item ->
                        when (item.itemId) {
                            R.id.action_share_item -> {
                                onShareClick(reaction)
                                true
                            }

                            R.id.action_edit_item -> {
                                onEditClick(reaction)
                                true
                            }

                            R.id.action_delete_item -> {
                                onDeleteClick(reaction)
                                true
                            }

                            R.id.action_report_item -> {
                                onReportClick(reaction)
                                true
                            }

                            else -> false
                        }
                    }
                    show()
                }
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
