package io.github.drumber.kitsune.ui.adapter.paging

import android.text.format.DateUtils
import android.widget.ImageView
import androidx.appcompat.widget.PopupMenu
import androidx.core.view.doOnPreDraw
import androidx.core.view.isVisible
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
import io.github.drumber.kitsune.util.ui.isTextTruncated

abstract class BasePostViewHolder(
    protected val binding: ItemPostBinding,
    private val glide: RequestManager,
    private val contentRenderer: PostContentRenderer?,
    private val nsfwAllowed: Boolean,
) : RecyclerView.ViewHolder(binding.root) {

    data class InteractionOverride(
        val isLiked: Boolean? = null,
        val likesCount: Int? = null,
        val commentsCount: Int? = null,
        val likerAvatars: List<String>? = null
    )

    protected abstract fun onPostClick(post: Post)
    protected abstract fun onAuthorClick(authorId: String)
    protected abstract fun isPostRevealed(post: Post): Boolean
    protected abstract fun onRevealPost(post: Post)
    protected abstract fun onMediaClick(post: Post)
    protected abstract fun onShareClick(post: Post)
    protected abstract fun onEditClick(post: Post)
    protected abstract fun onDeleteClick(post: Post)
    protected abstract fun onReportClick(post: Post)
    protected abstract fun getInteractionOverride(post: Post): InteractionOverride?
    protected abstract fun onLike(post: Post, isLiked: Boolean, likesCount: Int)
    protected abstract fun getLocalUserId(): String?

    open fun bind(post: Post) {
        binding.root.setOnDoubleTapListener(
            onSingleTap = { onPostClick(post) },
            onDoubleTap = { onLikeViaDoubleTap(post) }
        )

        val authorId = post.authorId
        if (authorId != null) {
            binding.ivAvatar.setOnClickListener { onAuthorClick(authorId) }
            binding.tvAuthor.setOnClickListener { onAuthorClick(authorId) }
        } else {
            binding.ivAvatar.setOnClickListener(null)
            binding.tvAuthor.setOnClickListener(null)
        }

        glide.load(post.authorAvatarUrl)
            .placeholder(R.drawable.ic_outline_person_24)
            .into(binding.ivAvatar)

        binding.tvAuthor.text = post.authorName
            ?: binding.root.context.getString(R.string.feed_unknown_user)

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
        val gated = needsWarning && !isPostRevealed(post)

        binding.layoutContentWarning.apply {
            isVisible = gated
            if (gated) {
                val isNsfwOnly = post.nsfw && !post.spoiler
                binding.tvWarningTitle.setText(
                    if (isNsfwOnly) {
                        R.string.feed_nsfw_warning_title
                    } else {
                        R.string.feed_spoiler_warning_title
                    }
                )
                setOnClickListener {
                    onRevealPost(post)
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

        bindReadMoreIndicator(post, gated)

        bindImagePreview(post, gated)

        EmbedBinder.bind(binding.embed, glide, post.embed, visible = !gated)

        PostMediaBinder.bind(binding.postMedia, glide, post, visible = true) {
            onMediaClick(post)
        }

        val override = getInteractionOverride(post)
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
            val override = getInteractionOverride(post)
            val current = override?.isLiked ?: false
            val currentCount = override?.likesCount ?: post.likesCount
            val targetLiked = !current
            val targetCount = (currentCount + if (targetLiked) 1 else -1).coerceAtLeast(0)
            onLike(post, targetLiked, targetCount)
        }

        binding.tvComments.text = commentsCount.toString()
        binding.tvComments.contentDescription =
            binding.root.context.getString(R.string.cd_comments_count, commentsCount)

        bindLikerAvatars(post)
    }

    /** Likes the post in response to a double tap. Does nothing if it is already liked. */
    private fun onLikeViaDoubleTap(post: Post) {
        val override = getInteractionOverride(post)
        val current = override?.isLiked ?: false
        if (current) return
        val currentCount = override?.likesCount ?: post.likesCount
        onLike(post, true, currentCount + 1)
    }

    private fun bindLikerAvatars(post: Post) {
        val avatarViews = listOf(
            binding.ivLiker1,
            binding.ivLiker2,
            binding.ivLiker3
        )
        val override = getInteractionOverride(post)
        val avatars = override?.likerAvatars.orEmpty()
        binding.layoutLikers.isVisible = avatars.isNotEmpty()
        avatarViews.forEach { it.isVisible = false }
        binding.tvLikerMore.isVisible = false
        if (avatars.isEmpty()) return

        avatarViews.forEachIndexed { index, imageView ->
            val url = avatars.getOrNull(index)
            imageView.isVisible = url != null
            if (url != null) loadAvatar(imageView, url)
        }
        val totalLikes = override?.likesCount ?: post.likesCount
        val remaining = totalLikes - avatarViews.size
        binding.tvLikerMore.apply {
            isVisible = remaining > 0
            if (remaining > 0) {
                text = context.getString(R.string.feed_likers_more, remaining)
            }
        }
    }

    /**
     * Shows a "Read more" affordance when the post content is truncated in the feed. Truncation
     * can only be measured after layout, so we defer the check until the [TextView] has been laid
     * out and inspect the last line's ellipsis count.
     */
    private fun bindReadMoreIndicator(post: Post, gated: Boolean) {
        val contentShown = !gated && !post.content.isNullOrBlank()
        binding.tvReadMore.isVisible = false
        binding.tvReadMore.setOnClickListener(
            if (contentShown) {
                android.view.View.OnClickListener { onPostClick(post) }
            } else {
                null
            }
        )
        if (!contentShown) return

        binding.tvContent.doOnPreDraw {
            binding.tvReadMore.isVisible = binding.tvContent.isTextTruncated()
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

    private fun bindOverflowMenu(post: Post) {
        val currentUserId = getLocalUserId()
        val isOwner = currentUserId != null && post.authorId == currentUserId

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

                setOnMenuItemClickListener { menuItem ->
                    when (menuItem.itemId) {
                        R.id.action_share_item -> {
                            onShareClick(post)
                            true
                        }

                        R.id.action_edit_item -> {
                            onEditClick(post)
                            true
                        }

                        R.id.action_delete_item -> {
                            onDeleteClick(post)
                            true
                        }

                        R.id.action_report_item -> {
                            onReportClick(post)
                            true
                        }

                        else -> false
                    }
                }
                show()
            }
        }
    }

    private fun loadAvatar(imageView: ImageView, url: String) {
        glide.load(url)
            .placeholder(R.drawable.ic_outline_person_24)
            .circleCrop()
            .into(imageView)
    }
}