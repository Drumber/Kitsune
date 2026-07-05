package io.github.drumber.kitsune.ui.adapter.paging

import android.text.format.DateUtils
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import androidx.appcompat.widget.PopupMenu
import androidx.core.view.isVisible
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.RequestManager
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import io.github.drumber.kitsune.R
import io.github.drumber.kitsune.data.presentation.model.feed.Post
import io.github.drumber.kitsune.databinding.ItemPostBinding
import io.github.drumber.kitsune.util.extensions.setOnDoubleTapListener
import io.github.drumber.kitsune.util.parseUtcDate
import io.github.drumber.kitsune.util.ui.EmbedBinder
import io.github.drumber.kitsune.util.ui.PostContentRenderer
import io.github.drumber.kitsune.util.ui.PostMediaBinder

class PostPagingAdapter(
    private val glide: RequestManager,
    private val contentRenderer: PostContentRenderer? = null,
    private val nsfwAllowed: Boolean = false,
    private val currentUserId: String? = null,
    private val listener: PostInteractionListener? = null
) : PagingDataAdapter<Post, PostPagingAdapter.PostViewHolder>(PostComparator) {

    private data class InteractionOverride(
        val isLiked: Boolean? = null,
        val likesCount: Int? = null,
        val commentsCount: Int? = null,
        val likerAvatars: List<String>? = null
    )

    private val overrides = mutableMapOf<String, InteractionOverride>()

    private val revealedIds = mutableSetOf<String>()

    /** Marks the given post's gated content as revealed and refreshes the item. */
    fun markRevealed(postId: String) {
        if (revealedIds.add(postId)) refreshItem(postId)
    }

    /** Overrides the like state of the post and refreshes the item. */
    fun setLikeState(postId: String, isLiked: Boolean, count: Int) {
        val current = overrides[postId] ?: InteractionOverride()
        overrides[postId] = current.copy(isLiked = isLiked, likesCount = count)
        refreshItem(postId)
    }

    /**
     * Applies a shared interaction override (like state and/or comment count) to the post and
     * refreshes the item if it is currently shown.
     */
    fun applyInteraction(
        postId: String,
        isLiked: Boolean?,
        likesCount: Int?,
        commentsCount: Int?,
        likerAvatars: List<String>? = null
    ) {
        val current = overrides[postId] ?: InteractionOverride()
        overrides[postId] = current.copy(
            isLiked = isLiked ?: current.isLiked,
            likesCount = likesCount ?: current.likesCount,
            commentsCount = commentsCount ?: current.commentsCount,
            likerAvatars = likerAvatars ?: current.likerAvatars
        )
        refreshItem(postId)
    }

    private fun refreshItem(postId: String) {
        val index = snapshot().items.indexOfFirst { it.id == postId }
        if (index != -1) notifyItemChanged(index)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {
        return PostViewHolder(
            ItemPostBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        )
    }

    override fun onBindViewHolder(holder: PostViewHolder, position: Int) {
        if (position >= itemCount) return
        getItem(position)?.let { holder.bind(it) }
    }

    inner class PostViewHolder(private val binding: ItemPostBinding) :
        RecyclerView.ViewHolder(binding.root) {

        /** Likes the post in response to a double tap. Does nothing if it is already liked. */
        private fun likeViaDoubleTap(post: Post) {
            val current = overrides[post.id]?.isLiked ?: false
            if (current) return
            val currentCount = overrides[post.id]?.likesCount ?: post.likesCount
            setLikeState(post.id, true, currentCount + 1)
            listener?.onLikeClick(post, true)
        }

        private fun bindOverflowMenu(post: Post) {
            val isOwner = currentUserId != null && post.authorId == currentUserId
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
                                listener?.onEditClick(post)
                                true
                            }

                            R.id.action_delete_item -> {
                                listener?.onDeleteClick(post)
                                true
                            }

                            else -> false
                        }
                    }
                    show()
                }
            }
        }

        fun bind(post: Post) {
            binding.root.setOnDoubleTapListener(
                onSingleTap = { listener?.onPostClick(binding.root, post) },
                onDoubleTap = { likeViaDoubleTap(post) }
            )

            glide.load(post.authorAvatarUrl)
                .placeholder(R.drawable.ic_outline_person_24)
                .circleCrop()
                .into(binding.ivAvatar)

            binding.tvAuthor.text = post.authorName
                ?: binding.root.context.getString(R.string.feed_unknown_user)

            val authorId = post.authorId
            val authorClickListener = if (authorId != null && listener != null) {
                android.view.View.OnClickListener { listener.onAuthorClick(authorId) }
            } else {
                null
            }
            binding.ivAvatar.setOnClickListener(authorClickListener)
            binding.tvAuthor.setOnClickListener(authorClickListener)

            bindOverflowMenu(post)

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

            val needsWarning = post.spoiler || (post.nsfw && !nsfwAllowed)
            val gated = needsWarning && post.id !in revealedIds

            binding.layoutContentWarning.apply {
                isVisible = gated
                if (gated) {
                    val isNsfwOnly = post.nsfw && !post.spoiler
                    binding.tvWarningTitle.setText(
                        if (isNsfwOnly) R.string.feed_nsfw_warning_title
                        else R.string.feed_spoiler_warning_title
                    )
                    setOnClickListener {
                        markRevealed(post.id)
                        listener?.onRevealClick(post)
                    }
                } else {
                    setOnClickListener(null)
                }
            }

            binding.tvContent.apply {
                isVisible = !gated && !post.content.isNullOrBlank()
                if (!gated) {
                    contentRenderer?.render(this, post.contentFormatted, post.content)
                        ?: run { text = post.content }
                }
            }

            bindImagePreview(post, gated)

            EmbedBinder.bind(binding.embed, glide, post.embed, visible = !gated)

            PostMediaBinder.bind(binding.postMedia, glide, post, visible = true) {
                listener?.onMediaClick(post)
            }

            val override = overrides[post.id]
            val isLiked = override?.isLiked ?: false
            val likesCount = override?.likesCount ?: post.likesCount
            val commentsCount = override?.commentsCount ?: post.commentsCount

            binding.tvLikes.text = likesCount.toString()
            binding.ivLike.setImageResource(
                if (isLiked) R.drawable.ic_favorite_24 else R.drawable.ic_favorite_border_24
            )
            binding.layoutLike.contentDescription = binding.root.context.getString(
                if (isLiked) R.string.cd_unlike_post else R.string.cd_like_post,
                likesCount
            )
            binding.layoutLike.setOnClickListener {
                val current = overrides[post.id]?.isLiked ?: false
                val currentCount = overrides[post.id]?.likesCount ?: post.likesCount
                val targetLiked = !current
                val targetCount = (currentCount + if (targetLiked) 1 else -1).coerceAtLeast(0)
                setLikeState(post.id, targetLiked, targetCount)
                listener?.onLikeClick(post, targetLiked)
            }

            binding.tvComments.text = commentsCount.toString()
            binding.tvComments.contentDescription =
                binding.root.context.getString(R.string.cd_comments_count, commentsCount)

            bindLikerAvatars(post)
        }

        private fun bindLikerAvatars(post: Post) {
            val avatarViews = listOf(
                binding.ivLiker1,
                binding.ivLiker2,
                binding.ivLiker3
            )
            val avatars = overrides[post.id]?.likerAvatars.orEmpty()
            binding.layoutLikers.isVisible = avatars.isNotEmpty()
            avatarViews.forEach { it.isVisible = false }
            binding.tvLikerMore.isVisible = false
            if (avatars.isEmpty()) return

            avatarViews.forEachIndexed { index, imageView ->
                val url = avatars.getOrNull(index)
                imageView.isVisible = url != null
                if (url != null) loadAvatar(imageView, url)
            }
            val totalLikes = overrides[post.id]?.likesCount ?: post.likesCount
            val remaining = totalLikes - avatarViews.size
            binding.tvLikerMore.apply {
                isVisible = remaining > 0
                if (remaining > 0) {
                    text = context.getString(R.string.feed_likers_more, remaining)
                }
            }
        }

        private fun bindImagePreview(post: Post, gated: Boolean) {
            val images = post.imageUrls
            val show = !gated && images.isNotEmpty()
            binding.layoutImage.isVisible = show
            if (!show) return

            val radius = (12 * binding.root.resources.displayMetrics.density).toInt()
            glide.load(images.first())
                .placeholder(R.drawable.ic_insert_photo_48)
                .transform(CenterCrop(), RoundedCorners(radius))
                .into(binding.ivImage)

            binding.tvImageCount.apply {
                isVisible = images.size > 1
                text = context.getString(R.string.feed_image_count_more, images.size - 1)
            }
        }

        private fun loadAvatar(imageView: ImageView, url: String) {
            glide.load(url)
                .placeholder(R.drawable.ic_outline_person_24)
                .circleCrop()
                .into(imageView)
        }

    }

    object PostComparator : DiffUtil.ItemCallback<Post>() {
        override fun areItemsTheSame(oldItem: Post, newItem: Post) = oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: Post, newItem: Post) = oldItem == newItem
    }

}
