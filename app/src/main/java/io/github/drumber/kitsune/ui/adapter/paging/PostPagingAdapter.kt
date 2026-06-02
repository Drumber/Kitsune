package io.github.drumber.kitsune.ui.adapter.paging

import android.text.format.DateUtils
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
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
import io.github.drumber.kitsune.ui.adapter.OnItemClickListener
import io.github.drumber.kitsune.util.parseUtcDate
import io.github.drumber.kitsune.util.ui.EmbedBinder
import io.github.drumber.kitsune.util.ui.PostContentRenderer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class PostPagingAdapter(
    private val glide: RequestManager,
    private val listener: OnItemClickListener<Post>? = null,
    private val scope: CoroutineScope? = null,
    private val avatarProvider: (suspend (Post) -> List<String>)? = null,
    private val onLikeClick: ((Post, Boolean) -> Unit)? = null,
    private val likeStateLoader: (suspend (Post) -> Unit)? = null,
    private val contentRenderer: PostContentRenderer? = null,
    private val nsfwAllowed: Boolean = false,
    private val onRevealClick: ((Post) -> Unit)? = null
) : PagingDataAdapter<Post, PostPagingAdapter.PostViewHolder>(PostComparator) {

    private data class InteractionOverride(
        val isLiked: Boolean? = null,
        val likesCount: Int? = null,
        val commentsCount: Int? = null
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
        commentsCount: Int?
    ) {
        val current = overrides[postId] ?: InteractionOverride()
        overrides[postId] = current.copy(
            isLiked = isLiked ?: current.isLiked,
            likesCount = likesCount ?: current.likesCount,
            commentsCount = commentsCount ?: current.commentsCount
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

    override fun onViewRecycled(holder: PostViewHolder) {
        super.onViewRecycled(holder)
        holder.clear()
    }

    inner class PostViewHolder(private val binding: ItemPostBinding) :
        RecyclerView.ViewHolder(binding.root) {

        private var avatarJob: Job? = null

        fun clear() {
            avatarJob?.cancel()
            avatarJob = null
        }

        fun bind(post: Post) {
            binding.root.setOnClickListener {
                listener?.onItemClick(binding.root, post)
            }

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
                        onRevealClick?.invoke(post)
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

            val override = overrides[post.id]
            val isLiked = override?.isLiked ?: false
            val likesCount = override?.likesCount ?: post.likesCount
            val commentsCount = override?.commentsCount ?: post.commentsCount

            binding.tvLikes.text = likesCount.toString()
            binding.ivLike.setImageResource(
                if (isLiked) R.drawable.ic_favorite_24 else R.drawable.ic_favorite_border_24
            )
            binding.layoutLike.setOnClickListener {
                val current = overrides[post.id]?.isLiked ?: false
                val currentCount = overrides[post.id]?.likesCount ?: post.likesCount
                val targetLiked = !current
                val targetCount = (currentCount + if (targetLiked) 1 else -1).coerceAtLeast(0)
                setLikeState(post.id, targetLiked, targetCount)
                onLikeClick?.invoke(post, targetLiked)
            }

            binding.tvComments.text = commentsCount.toString()

            scope?.let { s ->
                likeStateLoader?.let { loader -> s.launch { loader(post) } }
            }

            bindCommenterAvatars(post)
        }

        private fun bindCommenterAvatars(post: Post) {
            avatarJob?.cancel()
            val avatarViews = listOf(
                binding.ivCommenter1,
                binding.ivCommenter2,
                binding.ivCommenter3
            )
            binding.layoutCommenters.isVisible = false
            avatarViews.forEach { it.isVisible = false }

            if (post.commentsCount <= 0) return
            val scope = scope ?: return
            val avatarProvider = avatarProvider ?: return

            avatarJob = scope.launch {
                val avatars = avatarProvider(post)
                if (avatars.isEmpty()) return@launch
                binding.layoutCommenters.isVisible = true
                avatarViews.forEachIndexed { index, imageView ->
                    val url = avatars.getOrNull(index)
                    imageView.isVisible = url != null
                    if (url != null) loadAvatar(imageView, url)
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
