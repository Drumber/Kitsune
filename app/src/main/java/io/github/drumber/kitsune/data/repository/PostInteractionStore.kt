package io.github.drumber.kitsune.data.repository

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/**
 * In-memory store of post interaction state (like state and comment count) keyed by post id.
 *
 * Acts as a single source of truth shared between the feed and the post detail screen so that
 * liking a post or posting a comment in one place is reflected in the cached feed view without a
 * full reload.
 */
class PostInteractionStore {

    data class State(
        val isLiked: Boolean? = null,
        val likesCount: Int? = null,
        val commentsCount: Int? = null
    )

    private val _states = MutableStateFlow<Map<String, State>>(emptyMap())
    val states: StateFlow<Map<String, State>> = _states.asStateFlow()

    fun get(postId: String): State? = _states.value[postId]

    /** Updates the like state and like count for the given post. */
    fun setLikeState(postId: String, isLiked: Boolean, likesCount: Int) {
        _states.update { map ->
            val current = map[postId] ?: State()
            map + (postId to current.copy(isLiked = isLiked, likesCount = likesCount))
        }
    }

    /** Updates the comment count for the given post. */
    fun setCommentCount(postId: String, commentsCount: Int) {
        _states.update { map ->
            val current = map[postId] ?: State()
            map + (postId to current.copy(commentsCount = commentsCount))
        }
    }

}
