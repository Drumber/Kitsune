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
 *
 * The map is capped at [maxSize] to bound memory usage over a long session. Every update moves the
 * touched post to the most-recent position, so once the cap is exceeded the least-recently-touched
 * post is evicted first (LRU). Evicting a post's cached state is harmless: it just means the feed
 * falls back to the values from the last network load for that post.
 */
class PostInteractionStore(
    private val maxSize: Int = DEFAULT_MAX_SIZE
) {

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
        updateState(postId) { it.copy(isLiked = isLiked, likesCount = likesCount) }
    }

    /** Updates the comment count for the given post. */
    fun setCommentCount(postId: String, commentsCount: Int) {
        updateState(postId) { it.copy(commentsCount = commentsCount) }
    }

    private fun updateState(postId: String, transform: (State) -> State) {
        _states.update { map ->
            val current = map[postId] ?: State()
            // Re-insert last so the most-recently-touched post is evicted last, then trim to cap.
            val updated = LinkedHashMap<String, State>(map)
            updated.remove(postId)
            updated[postId] = transform(current)
            while (updated.size > maxSize) {
                updated.remove(updated.keys.first())
            }
            updated
        }
    }

    companion object {
        private const val DEFAULT_MAX_SIZE = 200
    }

}
