package io.github.drumber.kitsune.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.drumber.kitsune.data.common.exception.NoDataException
import io.github.drumber.kitsune.data.presentation.model.user.User
import io.github.drumber.kitsune.data.repository.FollowRepository
import io.github.drumber.kitsune.data.repository.UserRepository
import io.github.drumber.kitsune.domain.user.GetLocalUserIdUseCase
import io.github.drumber.kitsune.util.logE
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class UserProfileViewModel(
    private val userId: String,
    private val userRepository: UserRepository,
    private val followRepository: FollowRepository,
    private val getLocalUserId: GetLocalUserIdUseCase
) : ViewModel() {

    private val _userModel = MutableStateFlow<User?>(null)
    val userModel = _userModel.asStateFlow()

    private val _uiState = MutableStateFlow(UserProfileUiState())
    val uiState = _uiState.asStateFlow()

    /** Whether the viewed profile belongs to the currently logged-in user. */
    val isOwnProfile: Boolean
        get() = getLocalUserId() == userId

    init {
        loadUser()
        loadFollowState()
    }

    fun getUser(): User? = _userModel.value

    fun refreshUser() {
        _uiState.update { it.copy(isRefreshing = true) }
        loadUser()
        loadFollowState()
    }

    private fun loadUser() {
        viewModelScope.launch {
            try {
                val user = userRepository.fetchUser(userId, ProfileViewModel.FULL_USER_FILTER)
                    ?: throw NoDataException("Received data is null.")
                _userModel.update { user }
            } catch (e: Exception) {
                logE("Failed to fetch user profile.", e)
            } finally {
                _uiState.update { it.copy(isInitialLoading = false, isRefreshing = false) }
            }
        }
    }

    private fun loadFollowState() {
        val localUserId = getLocalUserId()
        if (localUserId == null || localUserId == userId) {
            _uiState.update { it.copy(canFollow = false) }
            return
        }
        viewModelScope.launch {
            try {
                val followId = followRepository.getFollowId(localUserId, userId)
                _uiState.update {
                    it.copy(
                        canFollow = true,
                        followId = followId,
                        isFollowing = followId != null
                    )
                }
            } catch (e: Exception) {
                logE("Failed to load follow state.", e)
            }
        }
    }

    fun toggleFollow() {
        val localUserId = getLocalUserId() ?: return
        if (localUserId == userId) return
        val state = _uiState.value
        if (state.isFollowProcessing) return

        viewModelScope.launch {
            _uiState.update { it.copy(isFollowProcessing = true) }
            try {
                if (state.isFollowing) {
                    val followId = state.followId
                    if (followId != null && followRepository.unfollow(followId)) {
                        _uiState.update {
                            it.copy(isFollowing = false, followId = null)
                        }
                    }
                } else {
                    val newId = followRepository.follow(localUserId, userId)
                    if (newId != null) {
                        _uiState.update {
                            it.copy(isFollowing = true, followId = newId)
                        }
                    }
                }
            } catch (e: Exception) {
                logE("Failed to toggle follow state.", e)
            } finally {
                _uiState.update { it.copy(isFollowProcessing = false) }
            }
        }
    }
}

data class UserProfileUiState(
    val isRefreshing: Boolean = false,
    val isInitialLoading: Boolean = true,
    val canFollow: Boolean = false,
    val isFollowing: Boolean = false,
    val isFollowProcessing: Boolean = false,
    val followId: String? = null
)
