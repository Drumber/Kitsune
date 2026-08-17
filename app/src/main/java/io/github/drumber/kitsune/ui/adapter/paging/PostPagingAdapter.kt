package io.github.drumber.kitsune.ui.adapter.paging

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import coil3.ImageLoader
import io.github.drumber.kitsune.data.presentation.model.feed.Post
import io.github.drumber.kitsune.databinding.ItemPostBinding
import io.github.drumber.kitsune.ui.adapter.paging.BasePostViewHolder.InteractionOverride
import io.github.drumber.kitsune.util.markwon.PostContentRenderer

class PostPagingAdapter(
    private val imageLoader: ImageLoader,
    private val contentRenderer: PostContentRenderer?,
    private val nsfwAllowed: Boolean,
    private val currentUserId: String?,
    private val listener: PostInteractionListener?
) : PagingDataAdapter<Post, PostPagingAdapter.PostViewHolder>(PostComparator) {

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
        val updated = current.copy(
            isLiked = isLiked ?: current.isLiked,
            likesCount = likesCount ?: current.likesCount,
            commentsCount = commentsCount ?: current.commentsCount,
            likerAvatars = likerAvatars ?: current.likerAvatars
        )
        if (current != updated) {
            overrides[postId] = updated
            refreshItem(postId)
        }
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

    inner class PostViewHolder(binding: ItemPostBinding) :
        BasePostViewHolder(binding, imageLoader, contentRenderer, nsfwAllowed) {

        override fun getLocalUserId() = currentUserId

        override fun onPostClick(post: Post) {
            listener?.onPostClick(binding.root, post)
        }

        override fun onAuthorClick(authorId: String) {
            listener?.onAuthorClick(authorId)
        }

        override fun isPostRevealed(post: Post): Boolean {
            return post.id in revealedIds
        }

        override fun onRevealPost(post: Post) {
            markRevealed(post.id)
            listener?.onRevealClick(post)
        }

        override fun onMediaClick(post: Post) {
            listener?.onMediaClick(post)
        }

        override fun onShareClick(post: Post) {
            listener?.onShareClick(post)
        }

        override fun onEditClick(post: Post) {
            listener?.onEditClick(post)
        }

        override fun onDeleteClick(post: Post) {
            listener?.onDeleteClick(post)
        }

        override fun onReportClick(post: Post) {
            listener?.onReportClick(post)
        }

        override fun getInteractionOverride(post: Post): InteractionOverride? = overrides[post.id]

        override fun onLike(
            post: Post,
            isLiked: Boolean,
            likesCount: Int
        ) {
            setLikeState(post.id, isLiked, likesCount)
            listener?.onLikeClick(post, isLiked)
        }
    }

    object PostComparator : DiffUtil.ItemCallback<Post>() {
        override fun areItemsTheSame(oldItem: Post, newItem: Post) = oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: Post, newItem: Post) = oldItem == newItem
    }
}
