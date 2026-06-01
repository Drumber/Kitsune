package io.github.drumber.kitsune.ui.adapter.paging

import android.text.format.DateUtils
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.RequestManager
import io.github.drumber.kitsune.R
import io.github.drumber.kitsune.data.presentation.model.comment.Comment
import io.github.drumber.kitsune.databinding.ItemCommentBinding
import io.github.drumber.kitsune.util.parseUtcDate

class CommentPagingAdapter(
    private val glide: RequestManager,
    private val onLikeClick: ((Comment) -> Unit)? = null
) : PagingDataAdapter<Comment, CommentPagingAdapter.CommentViewHolder>(CommentComparator) {

    private data class LikeState(val isLiked: Boolean, val count: Int)

    private val likeOverrides = mutableMapOf<String, LikeState>()

    /** Overrides the like state of the comment and refreshes the item. */
    fun setLikeState(commentId: String, isLiked: Boolean, count: Int) {
        likeOverrides[commentId] = LikeState(isLiked, count)
        val index = snapshot().items.indexOfFirst { it.id == commentId }
        if (index != -1) notifyItemChanged(index)
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

    inner class CommentViewHolder(private val binding: ItemCommentBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(comment: Comment) {
            glide.load(comment.authorAvatarUrl)
                .placeholder(R.drawable.ic_outline_person_24)
                .circleCrop()
                .into(binding.ivAvatar)

            binding.tvAuthor.text = comment.authorName
                ?: binding.root.context.getString(R.string.feed_unknown_user)

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
                text = comment.content
                isVisible = !comment.content.isNullOrBlank()
            }

            val override = likeOverrides[comment.id]
            val isLiked = override?.isLiked ?: comment.isLikedByMe
            val count = override?.count ?: comment.likesCount

            binding.tvLikes.text = count.toString()
            binding.ivLike.setImageResource(
                if (isLiked) R.drawable.ic_favorite_24 else R.drawable.ic_favorite_border_24
            )
            binding.layoutLike.setOnClickListener { onLikeClick?.invoke(comment) }
        }

    }

    object CommentComparator : DiffUtil.ItemCallback<Comment>() {
        override fun areItemsTheSame(oldItem: Comment, newItem: Comment) = oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: Comment, newItem: Comment) = oldItem == newItem
    }

}
