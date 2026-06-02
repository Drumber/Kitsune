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
 */
class ContentRevealStore {

    private val _revealed = MutableStateFlow<Set<String>>(emptySet())
    val revealed: StateFlow<Set<String>> = _revealed.asStateFlow()

    fun isRevealed(postId: String): Boolean = postId in _revealed.value

    fun reveal(postId: String) {
        _revealed.update { it + postId }
    }

}
