package io.github.drumber.kitsune.ui.postdetail

import android.text.format.DateUtils
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.bumptech.glide.RequestManager
import io.github.drumber.kitsune.R
import io.github.drumber.kitsune.data.presentation.model.feed.Post
import io.github.drumber.kitsune.databinding.ItemPostDetailHeaderBinding
import io.github.drumber.kitsune.util.parseUtcDate
import io.github.drumber.kitsune.util.ui.EmbedBinder
import io.github.drumber.kitsune.util.ui.PostContentRenderer

/** Single-item adapter rendering the full post card at the top of the post detail screen. */
class PostDetailHeaderAdapter(
    private val glide: RequestManager,
    private val onLikeClick: () -> Unit,
    private val contentRenderer: PostContentRenderer? = null,
    private val nsfwAllowed: Boolean = false,
    private val onRevealClick: () -> Unit = {}
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

            val needsWarning = post.spoiler || (post.nsfw && !nsfwAllowed)
            val gated = needsWarning && !revealed

            binding.layoutContentWarning.apply {
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

            EmbedBinder.bind(binding.embed, glide, post.embed, visible = !gated)

            binding.tvLikes.text = likesCount.toString()
            binding.ivLike.setImageResource(
                if (isLiked) R.drawable.ic_favorite_24 else R.drawable.ic_favorite_border_24
            )
            binding.layoutLike.setOnClickListener { onLikeClick() }

            binding.tvComments.text = post.commentsCount.toString()
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

            binding.vpImages.adapter = PostImagePagerAdapter(glide, images)

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
