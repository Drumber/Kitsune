package io.github.drumber.kitsune.data.repository

import io.github.drumber.kitsune.data.mapper.FeedMapper.toPost
import io.github.drumber.kitsune.data.presentation.model.feed.Post
import io.github.drumber.kitsune.data.common.Filter
import io.github.drumber.kitsune.data.source.network.feed.PostNetworkDataSource
import io.github.drumber.kitsune.data.source.network.feed.model.NetworkPost
import io.github.drumber.kitsune.data.source.network.feed.model.NetworkUpload
import io.github.drumber.kitsune.data.source.network.media.model.NetworkMedia
import io.github.drumber.kitsune.data.source.network.media.model.unit.NetworkMediaUnit
import io.github.drumber.kitsune.data.source.network.user.model.NetworkUser

class PostManagementRepository(
    private val postNetworkDataSource: PostNetworkDataSource
) {

    /**
     * Fetches a single post by id with the relationships needed to fully render it (author, media,
     * spoiled unit and image uploads). Used to load the complete post when only a partial copy is
     * available, e.g. when opening a post from a notification.
     */
    suspend fun getPost(postId: String): Post? {
        val filter = Filter()
            .include("user", "media", "spoiledUnit", "uploads")
        return postNetworkDataSource.getPost(postId, filter.options)?.toPost()
    }

    /**
     * Creates a new post on the current user's profile feed. Returns the created post, or `null`
     * when the server response is empty.
     *
     * @param media optional anime/manga the post is tagged with.
     * @param spoiledUnit optional episode/chapter the post spoils.
     * @param uploadIds ids of previously uploaded images to attach, in display order.
     */
    suspend fun postPost(
        userId: String,
        content: String?,
        spoiler: Boolean,
        nsfw: Boolean,
        media: NetworkMedia? = null,
        spoiledUnit: NetworkMediaUnit? = null,
        uploadIds: List<String> = emptyList(),
        targetUserId: String? = null
    ): Post? {
        val post = NetworkPost(
            id = null,
            content = content,
            spoiler = spoiler,
            nsfw = nsfw,
            user = NetworkUser(id = userId),
            targetUser = targetUserId?.let { NetworkUser(id = it) },
            media = media,
            spoiledUnit = spoiledUnit,
            uploads = uploadIds
                .takeIf { it.isNotEmpty() }
                ?.mapIndexed { index, id -> NetworkUpload(id = id, uploadOrder = index) }
        )
        return postNetworkDataSource.postPost(post)?.toPost()
    }

    /**
     * Updates an existing post owned by the user. Returns the updated post, or `null` when the
     * server response is empty.
     *
     * @param uploadIds ids of the post's images, in display order.
     */
    suspend fun updatePost(
        postId: String,
        content: String?,
        spoiler: Boolean,
        nsfw: Boolean,
        media: NetworkMedia? = null,
        spoiledUnit: NetworkMediaUnit? = null,
        uploadIds: List<String> = emptyList()
    ): Post? {
        val post = NetworkPost(
            id = postId,
            content = content,
            spoiler = spoiler,
            nsfw = nsfw,
            media = media,
            spoiledUnit = spoiledUnit,
            uploads = uploadIds
                .takeIf { it.isNotEmpty() }
                ?.mapIndexed { index, id -> NetworkUpload(id = id, uploadOrder = index) }
        )
        return postNetworkDataSource.updatePost(postId, post)?.toPost()
    }

    /** Deletes the post with the given id. */
    suspend fun deletePost(postId: String) {
        postNetworkDataSource.deletePost(postId)
    }

}
