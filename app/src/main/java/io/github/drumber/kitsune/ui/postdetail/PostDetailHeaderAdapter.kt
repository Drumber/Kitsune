package io.github.drumber.kitsune.ui.postdetail

import android.text.format.DateUtils
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.RequestManager
import io.github.drumber.kitsune.R
import io.github.drumber.kitsune.data.presentation.model.feed.Post
import io.github.drumber.kitsune.databinding.ItemPostDetailHeaderBinding
import io.github.drumber.kitsune.util.parseUtcDate

/** Single-item adapter rendering the full post card at the top of the post detail screen. */
class PostDetailHeaderAdapter(
    private val glide: RequestManager,
    private val onLikeClick: () -> Unit
) : RecyclerView.Adapter<PostDetailHeaderAdapter.HeaderViewHolder>() {

    private var post: Post? = null
    private var isLiked: Boolean = false
    private var likesCount: Int = 0

    fun setPost(post: Post) {
        this.post = post
        this.likesCount = post.likesCount
        notifyItemChanged(0)
    }

    fun setLikeState(isLiked: Boolean, count: Int) {
        this.isLiked = isLiked
        this.likesCount = count
        notifyItemChanged(0)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HeaderViewHolder {
        return HeaderViewHolder(
            ItemPostDetailHeaderBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        )
    }

    override fun onBindViewHolder(holder: HeaderViewHolder, position: Int) {
        post?.let { holder.bind(it) }
    }

    override fun getItemCount() = if (post != null) 1 else 0

    inner class HeaderViewHolder(private val binding: ItemPostDetailHeaderBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(post: Post) {
            glide.load(post.authorAvatarUrl)
                .placeholder(R.drawable.ic_outline_person_24)
                .circleCrop()
                .into(binding.ivAvatar)

            binding.tvAuthor.text = post.authorName
                ?: binding.root.context.getString(R.string.feed_unknown_user)

            binding.tvTimestamp.apply {
                val date = post.createdAt?.parseUtcDate()
                isVisible = date != null
                text = date?.let {
                    DateUtils.getRelativeTimeSpanString(
                        it.time,
                        System.currentTimeMillis(),
                        DateUtils.MINUTE_IN_MILLIS
                    )
                }
            }

            binding.tvMedia.apply {
                isVisible = !post.mediaTitle.isNullOrBlank()
                text = context.getString(R.string.feed_post_about, post.mediaTitle)
            }

            binding.tvSpoilerWarning.isVisible = post.spoiler

            binding.tvContent.apply {
                text = post.content
                isVisible = !post.content.isNullOrBlank()
            }

            binding.tvLikes.text = likesCount.toString()
            binding.ivLike.setImageResource(
                if (isLiked) R.drawable.ic_favorite_24 else R.drawable.ic_favorite_border_24
            )
            binding.layoutLike.setOnClickListener { onLikeClick() }

            binding.tvComments.text = post.commentsCount.toString()
        }

    }

}
