package io.github.drumber.kitsune.ui.adapter.paging

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.core.view.updateLayoutParams
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import coil3.ImageLoader
import io.github.drumber.kitsune.R
import io.github.drumber.kitsune.data.presentation.model.comment.Comment
import io.github.drumber.kitsune.databinding.ItemCommentBinding
import io.github.drumber.kitsune.util.extensions.toPx
import io.github.drumber.kitsune.util.markwon.PostContentRenderer

class CommentPagingAdapter(
    private val imageLoader: ImageLoader,
    private val contentRenderer: PostContentRenderer,
    private val currentUserId: String?,
    private val onLikeClick: (Comment) -> Unit,
    private val onReplyClick: (Comment) -> Unit,
    private val onViewAllRepliesClick: ((Comment) -> Unit)?,
    private val onEditClick: (Comment) -> Unit,
    private val onDeleteClick: (Comment) -> Unit,
    private val onAuthorClick: (String) -> Unit,
    private val onShareClick: (Comment) -> Unit,
    private val onReportClick: (Comment) -> Unit,
    private val onImageClick: (String) -> Unit,
) : PagingDataAdapter<Comment, CommentPagingAdapter.CommentViewHolder>(CommentComparator) {

    private val likeOverrides = mutableMapOf<String, BaseCommentViewHolder.InteractionOverride>()

    /**
     * Overrides the like state of the comment (top-level or reply) and refreshes its view. Replies
     * are rendered as nested views of their parent, so a reply like change refreshes the parent
     * item that owns it.
     */
    fun setLikeState(commentId: String, isLiked: Boolean, count: Int) {
        likeOverrides[commentId] = BaseCommentViewHolder.InteractionOverride(isLiked, count)
        val index = snapshot().items.indexOfFirst { comment ->
            comment.id == commentId || comment.replies.any { it.id == commentId }
        }
        if (index != -1) {
            notifyItemChanged(index)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CommentViewHolder {
        return CommentViewHolder(
            ItemCommentBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        )
    }

    override fun onBindViewHolder(holder: CommentViewHolder, position: Int) {
        if (position >= itemCount) return
        getItem(position)?.let { holder.bind(it) }
    }

    override fun onViewRecycled(holder: CommentViewHolder) {
        super.onViewRecycled(holder)
        holder.clear()
    }

    inner class CommentViewHolder(
        private val binding: ItemCommentBinding
    ) : BaseCommentViewHolder(
        imageLoader = imageLoader,
        contentRenderer = contentRenderer,
        onLikeClick = onLikeClick,
        onEditClick = onEditClick,
        onDeleteClick = onDeleteClick,
        onAuthorClick = onAuthorClick,
        onShareClick = onShareClick,
        onReportClick = onReportClick,
        onImageClick = onImageClick,
        itemView = binding.root
    ) {

        override fun getLocalUserId(): String? = currentUserId
        override fun getInteractionOverride(comment: Comment) = likeOverrides[comment.id]

        fun bind(comment: Comment) {
            bindCommentViews(binding, comment, isReply = false)
            renderReplies(binding, comment)
        }

        fun clear() {
            binding.layoutReplies.removeAllViews()
            binding.layoutReplies.isVisible = false
            binding.btnViewAllReplies.isVisible = false
        }

        private fun bindCommentViews(
            binding: ItemCommentBinding,
            comment: Comment,
            isReply: Boolean = false,
        ) {
            super.bindCommentViews(binding, comment)

            // Replies are capped at one level, so only top-level comments can be replied to.
            binding.tvReply.isVisible = !isReply
            binding.tvReply.setOnClickListener { onReplyClick(comment) }

            // Only top-level comments get a trailing divider; replies are nested with less indentation
            // and a smaller avatar so the thread reads as a clear, tighter group.
            binding.dividerComment.isVisible = !isReply
            binding.layoutCommentBody.setPaddingRelative(
                if (isReply) 0 else 16.toPx(),
                10.toPx(),
                16.toPx(),
                10.toPx()
            )
            val avatarSize = if (isReply) 30.toPx() else 36.toPx()
            binding.ivAvatar.updateLayoutParams {
                width = avatarSize
                height = avatarSize
            }
        }

        /**
         * Renders the preview of [comment]'s replies as nested views and, when the comment has more
         * replies than are previewed, a link that opens the full paginated replies screen.
         */
        private fun renderReplies(binding: ItemCommentBinding, comment: Comment) {
            binding.layoutReplies.removeAllViews()
            val replies = comment.replies
            binding.layoutReplies.isVisible = replies.isNotEmpty()
            if (replies.isNotEmpty()) {
                val inflater = LayoutInflater.from(binding.root.context)
                replies.forEach { reply ->
                    val replyBinding = ItemCommentBinding.inflate(inflater, binding.layoutReplies, false)
                    bindCommentViews(replyBinding, reply, isReply = true)
                    binding.layoutReplies.addView(replyBinding.root)
                }
            }

            val hasMore = comment.repliesCount > replies.size
            binding.btnViewAllReplies.isVisible = hasMore
            if (hasMore) {
                binding.btnViewAllReplies.text = binding.root.context.resources.getQuantityString(
                    R.plurals.comment_view_all_replies,
                    comment.repliesCount,
                    comment.repliesCount
                )
                binding.btnViewAllReplies.setOnClickListener { onViewAllRepliesClick?.invoke(comment) }
            } else {
                binding.btnViewAllReplies.setOnClickListener(null)
            }
        }
    }

    object CommentComparator : DiffUtil.ItemCallback<Comment>() {
        override fun areItemsTheSame(oldItem: Comment, newItem: Comment) = oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: Comment, newItem: Comment) = oldItem == newItem
    }

}
