package io.github.drumber.kitsune.ui.profile

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
    private val userIdOrSlug: String,
    private val userRepository: UserRepository,
    private val followRepository: FollowRepository,
    private val getLocalUserId: GetLocalUserIdUseCase
) : BaseProfileViewModel() {

    private var userId: String? = null

    private val _userModel = MutableStateFlow<User?>(null)
    override val userModel = _userModel.asStateFlow()

    private val _uiState = MutableStateFlow(UserProfileUiState())
    override val uiState = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            userId = if (userIdOrSlug.matches(Regex("^\\d+$"))) {
                userIdOrSlug
            } else {
                try {
                    requireNotNull(userRepository.fetchUserIdBySlug(userIdOrSlug)) {
                        "fetchUserIdBySlug returned null"
                    }
                } catch (e: Exception) {
                    logE("Failed to fetch userId for slug '$userIdOrSlug'.", e)
                    _uiState.update { it.copy(isInitialLoading = false, isRefreshing = false) }
                    null
                }
            }

            loadUser()
            loadFollowState()
        }
    }

    override fun refreshUser() {
        _uiState.update { it.copy(isRefreshing = true) }
        loadUser()
        loadFollowState()
    }

    private fun loadUser() {
        viewModelScope.launch {
            try {
                val userId = userId ?: return@launch
                val user = userRepository.fetchUser(userId, MyProfileViewModel.FULL_USER_FILTER)
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
        val userId = userId
        if (localUserId == null || userId == null || localUserId == userId) {
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
        val userId = userId ?: return
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
    override val isRefreshing: Boolean = false,
    override val isInitialLoading: Boolean = true,
    val canFollow: Boolean = false,
    val isFollowing: Boolean = false,
    val isFollowProcessing: Boolean = false,
    val followId: String? = null
) : ProfileUiState
