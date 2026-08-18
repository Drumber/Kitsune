package io.github.drumber.kitsune.ui.postdetail

import android.text.format.DateUtils
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.appcompat.widget.PopupMenu
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import coil3.ImageLoader
import coil3.load
import coil3.request.error
import coil3.request.fallback
import coil3.request.placeholder
import io.github.drumber.kitsune.R
import io.github.drumber.kitsune.data.presentation.model.feed.Post
import io.github.drumber.kitsune.databinding.ItemPostDetailHeaderBinding
import io.github.drumber.kitsune.util.extensions.setOnDoubleTapListener
import io.github.drumber.kitsune.util.markwon.PostContentRenderer
import io.github.drumber.kitsune.util.parseUtcDate
import io.github.drumber.kitsune.util.ui.EmbedBinder
import io.github.drumber.kitsune.util.ui.PostMediaBinder

/** Single-item adapter rendering the full post card at the top of the post detail screen. */
class PostDetailHeaderAdapter(
    private val imageLoader: ImageLoader,
    private val contentRenderer: PostContentRenderer?,
    private val nsfwAllowed: Boolean,
    private val currentUserId: String?,
    private val onLikeClick: () -> Unit,
    private val onRevealClick: () -> Unit,
    private val onMediaClick: (Post) -> Unit,
    private val onEditClick: (Post) -> Unit,
    private val onDeleteClick: (Post) -> Unit,
    private val onAuthorClick: (String) -> Unit,
    private val onImageClick: (String) -> Unit,
    private val onShareClick: (Post) -> Unit,
    private val onReportClick: (Post) -> Unit,
) : RecyclerView.Adapter<PostDetailHeaderAdapter.HeaderViewHolder>() {

    private var post: Post? = null
    private var isLiked: Boolean = false
    private var likesCount: Int = 0
    private var revealed: Boolean = false

    fun setPost(post: Post) {
        this.post = post
        this.likesCount = post.likesCount
        notifyItemChanged(0)
    }

    fun setRevealed(revealed: Boolean) {
        if (this.revealed == revealed) return
        this.revealed = revealed
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

        private var pageChangeCallback: ViewPager2.OnPageChangeCallback? = null

        fun bind(post: Post) {
            binding.root.setOnDoubleTapListener {
                if (!isLiked) onLikeClick()
            }

            binding.ivAvatar.load(post.authorAvatarUrl, imageLoader = imageLoader) {
                placeholder(R.drawable.ic_outline_person_24)
                error(R.drawable.ic_outline_person_24)
                fallback(R.drawable.ic_outline_person_24)
            }

            binding.tvAuthor.text = post.authorName
                ?: binding.root.context.getString(R.string.feed_unknown_user)

            val authorId = post.authorId
            val authorClickListener = if (authorId != null) {
                android.view.View.OnClickListener { onAuthorClick(authorId) }
            } else {
                null
            }
            binding.ivAvatar.setOnClickListener(authorClickListener)
            binding.tvAuthor.setOnClickListener(authorClickListener)

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
            val gated = needsWarning && !revealed

            binding.cardContentWarning.apply {
                isVisible = gated
                if (gated) {
                    val isNsfwOnly = post.nsfw && !post.spoiler
                    binding.tvWarningTitle.setText(
                        if (isNsfwOnly) R.string.feed_nsfw_warning_title
                        else R.string.feed_spoiler_warning_title
                    )
                    setOnClickListener { onRevealClick() }
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

            bindImageGallery(post, gated)

            EmbedBinder.bind(binding.embed, imageLoader, post.embed, visible = !gated)

            PostMediaBinder.bind(binding.postMedia, post, visible = true) {
                onMediaClick(post)
            }

            bindOverflowMenu(post)

            binding.tvLikes.text = likesCount.toString()
            binding.ivLike.setImageResource(
                if (isLiked) R.drawable.ic_favorite_24 else R.drawable.ic_favorite_border_24
            )
            binding.layoutLike.setOnClickListener { onLikeClick() }

            binding.tvComments.text = post.commentsCount.toString()
        }

        private fun bindOverflowMenu(post: Post) {
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

        private fun bindImageGallery(post: Post, gated: Boolean) {
            pageChangeCallback?.let { binding.vpImages.unregisterOnPageChangeCallback(it) }
            pageChangeCallback = null

            val images = post.imageUrls
            val show = !gated && images.isNotEmpty()
            binding.vpImages.isVisible = show
            binding.tvImageIndicator.isVisible = show && images.size > 1

            if (!show) {
                binding.vpImages.adapter = null
                return
            }

            binding.vpImages.adapter = PostImagePagerAdapter(imageLoader, images, onImageClick) { aspectRatio ->
                val pager = binding.vpImages
                val width = pager.width
                if (width > 0 && aspectRatio > 0f) {
                    val density = pager.resources.displayMetrics.density
                    val minHeight = (160 * density).toInt()
                    val maxHeight = (440 * density).toInt()
                    val target = (width / aspectRatio).toInt().coerceIn(minHeight, maxHeight)
                    if (pager.layoutParams.height != target) {
                        pager.layoutParams = pager.layoutParams.apply { height = target }
                    }
                }
            }

            if (images.size > 1) {
                binding.tvImageIndicator.text = binding.root.context.getString(
                    R.string.feed_image_indicator, 1, images.size
                )
                val callback = object : ViewPager2.OnPageChangeCallback() {
                    override fun onPageSelected(position: Int) {
                        binding.tvImageIndicator.text = binding.root.context.getString(
                            R.string.feed_image_indicator, position + 1, images.size
                        )
                    }
                }
                binding.vpImages.registerOnPageChangeCallback(callback)
                pageChangeCallback = callback
            }
        }

    }

}
