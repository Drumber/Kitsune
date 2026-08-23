package io.github.drumber.kitsune.ui.adapter.paging

import android.content.res.ColorStateList
import android.graphics.drawable.Drawable
import android.text.format.DateUtils
import android.view.View
import androidx.appcompat.widget.PopupMenu
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import androidx.vectordrawable.graphics.drawable.Animatable2Compat
import androidx.vectordrawable.graphics.drawable.AnimatedVectorDrawableCompat
import coil3.ImageLoader
import coil3.dispose
import coil3.load
import coil3.request.crossfade
import coil3.request.error
import coil3.request.fallback
import coil3.request.placeholder
import io.github.drumber.kitsune.R
import io.github.drumber.kitsune.data.presentation.model.comment.Comment
import io.github.drumber.kitsune.databinding.ItemCommentBinding
import io.github.drumber.kitsune.util.extensions.setOnDoubleTapListener
import io.github.drumber.kitsune.util.markwon.PostContentRenderer
import io.github.drumber.kitsune.util.parseUtcDate
import io.github.drumber.kitsune.util.ui.EmbedBinder

abstract class BaseCommentViewHolder(
    private val imageLoader: ImageLoader,
    private val contentRenderer: PostContentRenderer,
    private val onLikeClick: (Comment) -> Unit,
    private val onEditClick: (Comment) -> Unit,
    private val onDeleteClick: (Comment) -> Unit,
    private val onAuthorClick: (String) -> Unit,
    private val onShareClick: (Comment) -> Unit,
    private val onReportClick: (Comment) -> Unit,
    private val onImageClick: (String) -> Unit,
    itemView: View
) : RecyclerView.ViewHolder(itemView) {

    data class InteractionOverride(
        val isLiked: Boolean,
        val likesCount: Int,
    )

    protected abstract fun getLocalUserId(): String?
    protected abstract fun getInteractionOverride(comment: Comment): InteractionOverride?

    protected fun bindCommentViews(
        binding: ItemCommentBinding,
        comment: Comment,
    ) {
        binding.ivAvatar.load(comment.authorAvatarUrl, imageLoader = imageLoader) {
            placeholder(R.drawable.ic_outline_person_24)
            error(R.drawable.ic_outline_person_24)
            fallback(R.drawable.ic_outline_person_24)
        }

        binding.tvAuthor.text = comment.authorName ?: binding.root.context.getString(R.string.feed_unknown_user)

        val authorId = comment.authorId
        val authorClickListener = authorId?.let { id ->
            View.OnClickListener { onAuthorClick(id) }
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
            contentRenderer.render(this, comment.contentFormatted, comment.content)
        }

        binding.ivImage.apply {
            isVisible = !comment.imageUrl.isNullOrBlank()
            if (!comment.imageUrl.isNullOrBlank()) {
                load(comment.imageUrl, imageLoader = imageLoader) {
                    crossfade(false)
                    placeholder(null)
                }
            } else {
                dispose()
                setImageResource(R.drawable.default_placeholder)
            }

            setOnClickListener {
                if (!comment.imageUrl.isNullOrBlank()) {
                    onImageClick(comment.imageUrl)
                }
            }
        }

        EmbedBinder.bind(binding.embed, imageLoader, comment.embed, visible = true)

        val override = getInteractionOverride(comment)
        val isLiked = override?.isLiked ?: comment.isLikedByMe
        val count = override?.likesCount ?: comment.likesCount
        bindLikeRow(binding, isLiked, count)
        binding.layoutLike.setOnClickListener {
            val currentIsLiked = override?.isLiked ?: comment.isLikedByMe
            updateLikeIcon(binding, !currentIsLiked, isUserAction = true)
            onLikeClick(comment)
        }

        // Double tapping the comment likes it (Instagram-style). Does nothing if already liked.
        binding.layoutCommentBody.setOnDoubleTapListener {
            val liked = getInteractionOverride(comment)?.isLiked ?: comment.isLikedByMe
            if (!liked) {
                updateLikeIcon(binding, true, isUserAction = true)
                onLikeClick(comment)
            }
        }

        bindOverflowMenu(binding, comment)
    }

    private fun bindLikeRow(binding: ItemCommentBinding, isLiked: Boolean, count: Int) {
        binding.tvLikes.text = count.toString()
        updateLikeIcon(binding, isLiked)
    }

    private fun updateLikeIcon(binding: ItemCommentBinding, isLiked: Boolean, isUserAction: Boolean = false) {
        if (isLiked && isUserAction) {
            AnimatedVectorDrawableCompat.create(
                binding.root.context,
                R.drawable.animated_favorite
            )?.apply {
                binding.ivLike.setImageDrawable(this)
                registerAnimationCallback(object : Animatable2Compat.AnimationCallback() {
                    private var originalImageTint: ColorStateList? = null

                    override fun onAnimationStart(drawable: Drawable?) {
                        originalImageTint = binding.ivLike.imageTintList
                        binding.ivLike.imageTintList = null
                    }

                    override fun onAnimationEnd(drawable: Drawable?) {
                        if (binding.ivLike.drawable == this@apply) {
                            binding.ivLike.setImageResource(R.drawable.ic_favorite_24)
                        }
                        originalImageTint?.let { binding.ivLike.imageTintList = it }
                    }
                })
                start()
            }
        } else if (binding.ivLike.drawable !is AnimatedVectorDrawableCompat || !isLiked) {
            binding.ivLike.setImageResource(
                if (isLiked) R.drawable.ic_favorite_24 else R.drawable.ic_favorite_border_24
            )
        }
    }

    private fun bindOverflowMenu(binding: ItemCommentBinding, comment: Comment) {
        val currentUserId = getLocalUserId()
        val isOwner = currentUserId != null && comment.authorId == currentUserId

        binding.btnOverflow.setOnClickListener { anchor ->
            PopupMenu(anchor.context, anchor).apply {
                menuInflater.inflate(R.menu.feed_item_options_menu, menu)
                if (!isOwner) {
                    menu.removeItem(R.id.action_edit_item)
                    menu.removeItem(R.id.action_delete_item)
                }
                if (isOwner) {
                    menu.removeItem(R.id.action_report_item)
                }

                setOnMenuItemClickListener { menuItem ->
                    when (menuItem.itemId) {
                        R.id.action_share_item -> {
                            onShareClick(comment)
                            true
                        }

                        R.id.action_edit_item -> {
                            onEditClick(comment)
                            true
                        }

                        R.id.action_delete_item -> {
                            onDeleteClick(comment)
                            true
                        }

                        R.id.action_report_item -> {
                            onReportClick(comment)
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