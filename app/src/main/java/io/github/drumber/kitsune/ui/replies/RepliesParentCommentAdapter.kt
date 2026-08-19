package io.github.drumber.kitsune.ui.replies

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import coil3.ImageLoader
import io.github.drumber.kitsune.data.presentation.model.comment.Comment
import io.github.drumber.kitsune.databinding.ItemRepliesParentCommentBinding
import io.github.drumber.kitsune.ui.adapter.paging.BaseCommentViewHolder
import io.github.drumber.kitsune.util.markwon.PostContentRenderer

class RepliesParentCommentAdapter(
    private val imageLoader: ImageLoader,
    private val contentRenderer: PostContentRenderer,
    private val currentUserId: String?,
    private val onLikeClick: (Comment) -> Unit,
    private val onEditClick: (Comment) -> Unit,
    private val onDeleteClick: (Comment) -> Unit,
    private val onAuthorClick: (String) -> Unit,
    private val onShareClick: (Comment) -> Unit,
    private val onReportClick: (Comment) -> Unit,
    private val onImageClick: (String) -> Unit,
) : RecyclerView.Adapter<RepliesParentCommentAdapter.RepliesParentCommentViewHolder>() {

    private var comment: Comment? = null

    override fun getItemCount() = if (comment != null) 1 else 0

    fun setComment(comment: Comment?) {
        if (this.comment == comment) {
            return
        }
        this.comment = comment
        notifyDataSetChanged()
    }

    fun setLikeState(commentId: String, isLiked: Boolean, count: Int) {
        val parentComment = comment ?: return
        if (parentComment.id != commentId) return
        this.comment = parentComment.copy(
            isLikedByMe = isLiked,
            likesCount = count,
        )
        notifyItemChanged(0)
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): RepliesParentCommentViewHolder {
        val binding = ItemRepliesParentCommentBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return RepliesParentCommentViewHolder(binding)
    }

    override fun onBindViewHolder(
        holder: RepliesParentCommentViewHolder,
        position: Int
    ) {
        comment?.let { holder.bind(it) }
    }

    inner class RepliesParentCommentViewHolder(
        private val binding: ItemRepliesParentCommentBinding
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
        override fun getInteractionOverride(comment: Comment) = null

        fun bind(comment: Comment) {
            val header = binding.parentComment
            bindCommentViews(header, comment)

            // The pinned header only represents the parent comment, so hide list-only affordances.
            header.tvReply.isVisible = false
            header.layoutReplies.isVisible = false
            header.btnViewAllReplies.isVisible = false
            header.dividerComment.isVisible = false
        }
    }
}
