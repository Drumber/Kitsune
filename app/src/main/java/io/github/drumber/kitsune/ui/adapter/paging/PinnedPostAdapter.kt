package io.github.drumber.kitsune.ui.adapter.paging

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.appcompat.widget.PopupMenu
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.RequestManager
import io.github.drumber.kitsune.R
import io.github.drumber.kitsune.data.presentation.model.feed.Post
import io.github.drumber.kitsune.databinding.ItemPostBinding
import io.github.drumber.kitsune.ui.adapter.paging.BasePostViewHolder.InteractionOverride
import io.github.drumber.kitsune.util.ui.PostContentRenderer

/** Single-item adapter rendering a pinned profile post above a feed. */
class PinnedPostAdapter(
    private val glide: RequestManager,
    private val contentRenderer: PostContentRenderer? = null,
    private val nsfwAllowed: Boolean = false,
    private val currentUserId: String? = null,
    private val listener: PostInteractionListener? = null
) : RecyclerView.Adapter<PinnedPostAdapter.PinnedPostViewHolder>() {

    private var post: Post? = null

    private var interactionOverride = InteractionOverride()

    private var revealed = false

    fun setPost(post: Post?) {
        val isNewPost = this.post?.id != post?.id
        this.post = post
        if (isNewPost) {
            interactionOverride = InteractionOverride()
            revealed = false
        }
        notifyDataSetChanged()
    }

    /** Overrides the like state of the post and refreshes the item. */
    fun setLikeState(postId: String, isLiked: Boolean, count: Int) {
        if (postId != post?.id) return
        interactionOverride = interactionOverride.copy(isLiked = isLiked, likesCount = count)
        notifyItemChanged(0)
    }

    /**
     * Applies a shared interaction override (like state and/or comment count) to the pinned post.
     */
    fun applyInteraction(
        postId: String,
        isLiked: Boolean?,
        likesCount: Int?,
        commentsCount: Int?,
        likerAvatars: List<String>? = null
    ) {
        if (postId != post?.id) return
        interactionOverride = interactionOverride.copy(
            isLiked = isLiked ?: interactionOverride.isLiked,
            likesCount = likesCount ?: interactionOverride.likesCount,
            commentsCount = commentsCount ?: interactionOverride.commentsCount,
            likerAvatars = likerAvatars ?: interactionOverride.likerAvatars
        )
        notifyItemChanged(0)
    }

    /** Marks the pinned post's gated content as revealed and refreshes the item. */
    fun markRevealed(postId: String) {
        if (postId != post?.id || revealed) return
        revealed = true
        notifyItemChanged(0)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PinnedPostViewHolder {
        return PinnedPostViewHolder(
            ItemPostBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        )
    }

    override fun onBindViewHolder(holder: PinnedPostViewHolder, position: Int) {
        post?.let { holder.bind(it) }
    }

    override fun getItemCount() = if (post != null) 1 else 0

    inner class PinnedPostViewHolder(binding: ItemPostBinding) :
        BasePostViewHolder(binding, glide, contentRenderer, nsfwAllowed) {

        override fun onPostClick(post: Post) {
            listener?.onPostClick(binding.root, post)
        }

        override fun onAuthorClick(authorId: String) {
            listener?.onAuthorClick(authorId)
        }

        override fun isPostRevealed(post: Post): Boolean {
            return revealed
        }

        override fun onRevealPost(post: Post) {
            markRevealed(post.id)
            listener?.onRevealClick(post)
        }

        override fun onMediaClick(post: Post) {
            listener?.onMediaClick(post)
        }

        override fun getInteractionOverride(post: Post): InteractionOverride = interactionOverride

        override fun onLike(
            post: Post,
            isLiked: Boolean,
            likesCount: Int
        ) {
            setLikeState(post.id, isLiked, likesCount)
            listener?.onLikeClick(post, isLiked)
        }

        override fun bind(post: Post) {
            binding.layoutPinnedLabel.isVisible = true
            super.bind(post)
        }

        override fun onBindOverflowMenu(post: Post) {
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
    }
}
