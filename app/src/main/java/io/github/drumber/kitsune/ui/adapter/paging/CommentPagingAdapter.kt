package io.github.drumber.kitsune.ui.adapter.paging

import android.text.format.DateUtils
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.appcompat.widget.PopupMenu
import androidx.core.view.isVisible
import androidx.core.view.updateLayoutParams
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.RequestManager
import io.github.drumber.kitsune.R
import io.github.drumber.kitsune.data.presentation.model.comment.Comment
import io.github.drumber.kitsune.databinding.ItemCommentBinding
import io.github.drumber.kitsune.util.extensions.setOnDoubleTapListener
import io.github.drumber.kitsune.util.parseUtcDate
import io.github.drumber.kitsune.util.ui.EmbedBinder
import io.github.drumber.kitsune.util.ui.PostContentRenderer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class CommentPagingAdapter(
    private val glide: RequestManager,
    private val onLikeClick: ((Comment) -> Unit)? = null,
    private val contentRenderer: PostContentRenderer? = null,
    private val scope: CoroutineScope? = null,
    private val repliesProvider: (suspend (Comment) -> List<Comment>)? = null,
    private val onReplyClick: ((Comment) -> Unit)? = null,
    private val currentUserId: String? = null,
    private val onEditClick: ((Comment) -> Unit)? = null,
    private val onDeleteClick: ((Comment) -> Unit)? = null,
    private val onAuthorClick: ((String) -> Unit)? = null
) : PagingDataAdapter<Comment, CommentPagingAdapter.CommentViewHolder>(CommentComparator) {

    private data class LikeState(val isLiked: Boolean, val count: Int)

    private val likeOverrides = mutableMapOf<String, LikeState>()

    // Currently displayed reply views keyed by comment id, so reply like state can be refreshed
    // without rebuilding the parent comment (replies are not part of the paging list).
    private val replyBindings = mutableMapOf<String, ItemCommentBinding>()

    /** Overrides the like state of the comment (top-level or reply) and refreshes its view. */
    fun setLikeState(commentId: String, isLiked: Boolean, count: Int) {
        likeOverrides[commentId] = LikeState(isLiked, count)
        val index = snapshot().items.indexOfFirst { it.id == commentId }
        if (index != -1) {
            notifyItemChanged(index)
            return
        }
        replyBindings[commentId]?.let { bindLikeRow(it, isLiked, count) }
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

    private fun bindCommentViews(
        binding: ItemCommentBinding,
        comment: Comment,
        isReply: Boolean
    ) {
        glide.load(comment.authorAvatarUrl)
            .placeholder(R.drawable.ic_outline_person_24)
            .circleCrop()
            .into(binding.ivAvatar)

        binding.tvAuthor.text = comment.authorName
            ?: binding.root.context.getString(R.string.feed_unknown_user)

        val authorId = comment.authorId
        val authorClickListener = if (authorId != null && onAuthorClick != null) {
            android.view.View.OnClickListener { onAuthorClick.invoke(authorId) }
        } else {
            null
        }
        binding.ivAvatar.setOnClickListener(authorClickListener)
        binding.tvAuthor.setOnClickListener(authorClickListener)

        binding.tvTimestamp.apply {
            val date = comment.createdAt?.parseUtcDate()
            isVisible = date != null
            text = date?.let {
                DateUtils.getRelativeTimeSpanString(
                    it.time,
                    System.currentTimeMillis(),
                    DateUtils.MINUTE_IN_MILLIS
                )
            }
        }

        binding.tvContent.apply {
            isVisible = !comment.content.isNullOrBlank()
            contentRenderer?.render(this, comment.contentFormatted, comment.content)
                ?: run { text = comment.content }
        }

        binding.ivImage.apply {
            isVisible = !comment.imageUrl.isNullOrBlank()
            if (!comment.imageUrl.isNullOrBlank()) {
                glide.load(comment.imageUrl)
                    .placeholder(R.drawable.ic_insert_photo_48)
                    .into(this)
            } else {
                glide.clear(this)
            }
        }

        EmbedBinder.bind(binding.embed, glide, comment.embed, visible = true)

        val override = likeOverrides[comment.id]
        val isLiked = override?.isLiked ?: comment.isLikedByMe
        val count = override?.count ?: comment.likesCount
        bindLikeRow(binding, isLiked, count)
        binding.layoutLike.setOnClickListener { onLikeClick?.invoke(comment) }

        // Double tapping the comment likes it (Instagram-style). Does nothing if already liked.
        binding.layoutCommentBody.setOnDoubleTapListener {
            val liked = likeOverrides[comment.id]?.isLiked ?: comment.isLikedByMe
            if (!liked) onLikeClick?.invoke(comment)
        }

        // Replies are capped at one level, so only top-level comments can be replied to.
        binding.tvReply.isVisible = !isReply && onReplyClick != null
        binding.tvReply.setOnClickListener { onReplyClick?.invoke(comment) }

        // Only top-level comments get a trailing divider; replies are nested with less indentation
        // and a smaller avatar so the thread reads as a clear, tighter group.
        binding.dividerComment.isVisible = !isReply
        val density = binding.root.resources.displayMetrics.density
        fun dp(value: Int) = (value * density).toInt()
        binding.layoutCommentBody.setPaddingRelative(
            if (isReply) 0 else dp(16),
            dp(10),
            dp(16),
            dp(10)
        )
        val avatarSize = dp(if (isReply) 30 else 36)
        binding.ivAvatar.updateLayoutParams {
            width = avatarSize
            height = avatarSize
        }

        bindOverflowMenu(binding, comment)
    }

    private fun bindOverflowMenu(binding: ItemCommentBinding, comment: Comment) {
        val isOwner = currentUserId != null && comment.authorId == currentUserId
        binding.btnOverflow.isVisible = isOwner
        if (!isOwner) {
            binding.btnOverflow.setOnClickListener(null)
            return
        }
        binding.btnOverflow.setOnClickListener { anchor ->
            PopupMenu(anchor.context, anchor).apply {
                menuInflater.inflate(R.menu.feed_item_options_menu, menu)
                setOnMenuItemClickListener { menuItem ->
                    when (menuItem.itemId) {
                        R.id.action_edit_item -> {
                            onEditClick?.invoke(comment)
                            true
                        }

                        R.id.action_delete_item -> {
                            onDeleteClick?.invoke(comment)
                            true
                        }

                        else -> false
                    }
                }
                show()
            }
        }
    }

    private fun bindLikeRow(binding: ItemCommentBinding, isLiked: Boolean, count: Int) {
        binding.tvLikes.text = count.toString()
        binding.ivLike.setImageResource(
            if (isLiked) R.drawable.ic_favorite_24 else R.drawable.ic_favorite_border_24
        )
    }

    inner class CommentViewHolder(private val binding: ItemCommentBinding) :
        RecyclerView.ViewHolder(binding.root) {

        private var replyJob: Job? = null
        private val boundReplyIds = mutableListOf<String>()

        fun bind(comment: Comment) {
            bindCommentViews(binding, comment, isReply = false)
            bindReplies(comment)
        }

        fun clear() {
            replyJob?.cancel()
            replyJob = null
            boundReplyIds.forEach { replyBindings.remove(it) }
            boundReplyIds.clear()
            binding.layoutReplies.removeAllViews()
            binding.layoutReplies.isVisible = false
        }

        private fun bindReplies(comment: Comment) {
            replyJob?.cancel()
            boundReplyIds.forEach { replyBindings.remove(it) }
            boundReplyIds.clear()
            binding.layoutReplies.removeAllViews()
            binding.layoutReplies.isVisible = false

            if (comment.repliesCount <= 0) return
            val scope = scope ?: return
            val provider = repliesProvider ?: return

            replyJob = scope.launch {
                val replies = provider(comment)
                if (replies.isEmpty()) return@launch
                binding.layoutReplies.isVisible = true
                val inflater = LayoutInflater.from(binding.root.context)
                replies.forEach { reply ->
                    val replyBinding = ItemCommentBinding.inflate(
                        inflater, binding.layoutReplies, false
                    )
                    bindCommentViews(replyBinding, reply, isReply = true)
                    replyBinding.layoutReplies.isVisible = false
                    binding.layoutReplies.addView(replyBinding.root)
                    replyBindings[reply.id] = replyBinding
                    boundReplyIds.add(reply.id)
                }
            }
        }

    }

    object CommentComparator : DiffUtil.ItemCallback<Comment>() {
        override fun areItemsTheSame(oldItem: Comment, newItem: Comment) = oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: Comment, newItem: Comment) = oldItem == newItem
    }

}
