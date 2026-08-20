package io.github.drumber.kitsune.data.repository

import io.github.drumber.kitsune.data.common.Filter
import io.github.drumber.kitsune.data.mapper.ImageMapper.toImage
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

    /**
     * Returns the current user's like ids for the given posts, keyed by post id, resolved in a
     * single request. Posts the user has not liked are absent from the map. The post resources are
     * requested with a minimal sparse fieldset so only their ids and one small attribute are
     * transferred (the API rejects an empty `fields[posts]=` value with HTTP 400).
     */
    suspend fun getMyPostLikeIds(postIds: List<String>, userId: String): Map<String, String> {
        if (postIds.isEmpty()) return emptyMap()
        val filter = Filter()
            .filter("postId", postIds.joinToString(","))
            .filter("userId", userId)
            .include("post")
            .fields("posts", "createdAt")
            .pageLimit(postIds.size)
        return postLikeNetworkDataSource.getPostLikes(filter)
            .mapNotNull { like ->
                val postId = like.post?.id ?: return@mapNotNull null
                val likeId = like.id ?: return@mapNotNull null
                postId to likeId
            }
            .toMap()
    }

    /** Returns up to [limit] distinct avatar urls of users who liked the given post. */
    suspend fun getTopLikerAvatars(postId: String, limit: Int = 3): List<String> {
        val filter = Filter()
            .filter("postId", postId)
            .include("user")
            .fields("users", "avatar")
            .pageLimit(limit * 4)
        return postLikeNetworkDataSource.getPostLikes(filter)
            .mapNotNull { it.user }
            .distinctBy { it.id }
            .mapNotNull { it.avatar?.toImage()?.smallOrHigher() }
            .take(limit)
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
