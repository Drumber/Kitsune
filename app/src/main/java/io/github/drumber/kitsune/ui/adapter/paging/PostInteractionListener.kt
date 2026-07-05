package io.github.drumber.kitsune.ui.adapter.paging

import android.view.View
import io.github.drumber.kitsune.data.presentation.model.feed.Post

/**
 * Consolidated callback surface for user interactions on feed posts rendered by
 * [PostPagingAdapter]. Every method has a no-op default so a host only needs to override the
 * interactions it actually supports.
 */
interface PostInteractionListener {

    /** Called when the post body is tapped. */
    fun onPostClick(view: View, post: Post) {}

    /** Called when the like control is activated (button tap or double tap on the post). */
    fun onLikeClick(post: Post, targetLiked: Boolean) {}

    /** Called when a gated (spoiler/NSFW) post is revealed. */
    fun onRevealClick(post: Post) {}

    /** Called when the media attached to a post is tapped. */
    fun onMediaClick(post: Post) {}

    /** Called when the owner chooses to edit their post. */
    fun onEditClick(post: Post) {}

    /** Called when the owner chooses to delete their post. */
    fun onDeleteClick(post: Post) {}

    /** Called when the post author's name or avatar is tapped. */
    fun onAuthorClick(userId: String) {}
}
