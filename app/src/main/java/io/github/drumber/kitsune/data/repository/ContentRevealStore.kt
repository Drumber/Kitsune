package io.github.drumber.kitsune.data.repository

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/**
 * In-memory store of post ids whose spoiler/NSFW content the user has chosen to reveal.
 *
 * Reveal decisions are remembered for the lifetime of the app session and shared between the feed
 * and the post detail screen, so revealing gated content in one place keeps it revealed elsewhere
 * (and when scrolling back to it) without persisting across app restarts.
 *
 * The set is capped at [maxSize] to bound memory usage over a long session. Because [reveal] only
 * ever appends ids, insertion order matches recency, so once the cap is exceeded the oldest reveals
 * are evicted first (least-recently-revealed). Re-gating an evicted post is harmless: the user can
 * simply reveal it again.
 */
class ContentRevealStore(
    private val maxSize: Int = DEFAULT_MAX_SIZE
) {

    private val _revealed = MutableStateFlow<Set<String>>(emptySet())
    val revealed: StateFlow<Set<String>> = _revealed.asStateFlow()

    fun reveal(postId: String) {
        _revealed.update { current ->
            if (postId in current) return@update current
            val updated = current + postId
            if (updated.size <= maxSize) {
                updated
            } else {
                updated.drop(updated.size - maxSize).toSet()
            }
        }
    }

    companion object {
        private const val DEFAULT_MAX_SIZE = 200
    }

}
