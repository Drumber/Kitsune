package io.github.drumber.kitsune.ui.replies

import android.text.format.DateUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.RequestManager
import io.github.drumber.kitsune.R
import io.github.drumber.kitsune.data.presentation.model.comment.Comment
import io.github.drumber.kitsune.databinding.ItemRepliesParentCommentBinding
import io.github.drumber.kitsune.util.parseUtcDate
import io.github.drumber.kitsune.util.ui.EmbedBinder
import io.github.drumber.kitsune.util.ui.PostContentRenderer

class RepliesParentCommentAdapter(
    private val glide: RequestManager,
    private val contentRenderer: PostContentRenderer,
    private val onAuthorClicked: (String) -> Unit,
    private val onLikeClicked: (Comment) -> Unit,
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
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(comment: Comment) {
            val header = binding.parentComment

            glide
                .load(comment.authorAvatarUrl)
                .placeholder(R.drawable.ic_outline_person_24)
                .circleCrop()
                .into(header.ivAvatar)

            header.tvAuthor.text = comment.authorName ?: binding.root.context.getString(R.string.feed_unknown_user)

            val authorId = comment.authorId
            val authorClickListener = authorId?.let { id ->
                View.OnClickListener { onAuthorClicked(id) }
            }
            header.ivAvatar.setOnClickListener(authorClickListener)
            header.tvAuthor.setOnClickListener(authorClickListener)

            header.tvTimestamp.apply {
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

            header.tvContent.apply {
                isVisible = !comment.content.isNullOrBlank()
                contentRenderer.render(this, comment.contentFormatted, comment.content)
            }

            header.ivImage.apply {
                isVisible = !comment.imageUrl.isNullOrBlank()
                if (!comment.imageUrl.isNullOrBlank()) {
                    glide
                        .load(comment.imageUrl)
                        .placeholder(R.drawable.ic_insert_photo_48)
                        .into(this)
                }
            }

            EmbedBinder.bind(header.embed, glide, comment.embed, visible = true)

            bindParentLikeRow(comment.isLikedByMe, comment.likesCount)
            header.layoutLike.setOnClickListener { onLikeClicked(comment) }

            // The pinned header only represents the parent comment, so hide list-only affordances.
            header.tvReply.isVisible = false
            header.btnOverflow.isVisible = false
            header.layoutReplies.isVisible = false
            header.tvViewAllReplies.isVisible = false
            header.dividerComment.isVisible = false
        }

        private fun bindParentLikeRow(isLiked: Boolean, count: Int) {
            val header = binding.parentComment
            header.tvLikes.text = count.toString()
            header.ivLike.setImageResource(
                if (isLiked) R.drawable.ic_favorite_24 else R.drawable.ic_favorite_border_24
            )
        }
    }
}