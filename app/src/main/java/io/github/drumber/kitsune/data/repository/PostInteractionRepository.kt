package io.github.drumber.kitsune.data.repository

import io.github.drumber.kitsune.data.common.Filter
import io.github.drumber.kitsune.data.source.network.feed.PostLikeNetworkDataSource
import io.github.drumber.kitsune.data.source.network.feed.model.NetworkPost
import io.github.drumber.kitsune.data.source.network.feed.model.NetworkPostLike
import io.github.drumber.kitsune.data.source.network.user.model.NetworkUser

class PostInteractionRepository(
    private val postLikeNetworkDataSource: PostLikeNetworkDataSource
) {

    /** Returns the id of the current user's like on the given post, or null if not liked. */
    suspend fun getMyPostLikeId(postId: String, userId: String): String? {
        val filter = Filter()
            .filter("postId", postId)
            .filter("userId", userId)
            .pageLimit(1)
        return postLikeNetworkDataSource.getPostLikes(filter).firstOrNull()?.id
    }

    /** Likes the given post on behalf of the user. Returns the created like id, or null on failure. */
    suspend fun likePost(postId: String, userId: String): String? {
        val like = NetworkPostLike(
            id = null,
            post = NetworkPost(id = postId),
            user = NetworkUser(id = userId)
        )
        return postLikeNetworkDataSource.postPostLike(like)?.id
    }

    /** Removes the like with the given id from a post. */
    suspend fun unlikePost(likeId: String) {
        postLikeNetworkDataSource.deletePostLike(likeId)
    }

}
