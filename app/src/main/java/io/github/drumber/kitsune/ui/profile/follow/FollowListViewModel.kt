package io.github.drumber.kitsune.ui.profile.follow

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import io.github.drumber.kitsune.data.presentation.model.user.FollowUser
import io.github.drumber.kitsune.data.repository.FollowListType
import io.github.drumber.kitsune.data.repository.FollowRepository
import io.github.drumber.kitsune.domain.user.GetLocalUserIdUseCase
import io.github.drumber.kitsune.util.logE
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class FollowListViewModel(
    private val userId: String,
    private val listType: FollowListType,
    private val followRepository: FollowRepository,
    private val getLocalUserId: GetLocalUserIdUseCase
) : ViewModel() {

    val type: FollowListType get() = listType

    private val localUserId: String? get() = getLocalUserId()

    val users: Flow<PagingData<FollowUser>> =
        followRepository.followListPager(userId, listType).cachedIn(viewModelScope)

    private val _followStates = MutableStateFlow<Map<String, FollowButtonState>>(emptyMap())
    val followStates = _followStates.asStateFlow()

    /** Whether the button should be shown for the given listed user. */
    fun showButtonFor(listedUserId: String): Boolean {
        val local = localUserId ?: return false
        return local != listedUserId
    }

    /**
     * Lazily resolves whether the logged-in user follows [listedUserId]. No-op if already resolved
     * or in progress, or if the button is not applicable.
     */
    fun resolveFollowState(listedUserId: String) {
        val local = localUserId ?: return
        if (local == listedUserId) return
        if (_followStates.value[listedUserId]?.isResolved == true) return
        if (_followStates.value[listedUserId]?.isProcessing == true) return

        viewModelScope.launch {
            try {
                val followId = followRepository.getFollowId(local, listedUserId)
                updateState(listedUserId) {
                    it.copy(
                        isResolved = true,
                        isFollowing = followId != null,
                        followId = followId
                    )
                }
            } catch (e: Exception) {
                logE("Failed to resolve follow state for user '$listedUserId'.", e)
            }
        }
    }

    fun toggleFollow(listedUserId: String) {
        val local = localUserId ?: return
        if (local == listedUserId) return

        val current = _followStates.value[listedUserId] ?: FollowButtonState()
        if (current.isProcessing) return

        viewModelScope.launch {
            updateState(listedUserId) { it.copy(isProcessing = true) }
            try {
                if (current.isFollowing) {
                    val followId = current.followId
                    if (followId != null && followRepository.unfollow(followId)) {
                        updateState(listedUserId) {
                            it.copy(isFollowing = false, followId = null, isResolved = true)
                        }
                    }
                } else {
                    val newId = followRepository.follow(local, listedUserId)
                    if (newId != null) {
                        updateState(listedUserId) {
                            it.copy(isFollowing = true, followId = newId, isResolved = true)
                        }
                    }
                }
            } catch (e: Exception) {
                logE("Failed to toggle follow state for user '$listedUserId'.", e)
            } finally {
                updateState(listedUserId) { it.copy(isProcessing = false) }
            }
        }
    }

    private inline fun updateState(
        listedUserId: String,
        transform: (FollowButtonState) -> FollowButtonState
    ) {
        _followStates.update { states ->
            val current = states[listedUserId] ?: FollowButtonState()
            states + (listedUserId to transform(current))
        }
    }
}

data class FollowButtonState(
    val isResolved: Boolean = false,
    val isFollowing: Boolean = false,
    val isProcessing: Boolean = false,
    val followId: String? = null
)
