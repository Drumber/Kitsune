package io.github.drumber.kitsune.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.drumber.kitsune.constants.Defaults
import io.github.drumber.kitsune.data.common.Filter
import io.github.drumber.kitsune.data.common.exception.NoDataException
import io.github.drumber.kitsune.data.mapper.UserMapper.toUser
import io.github.drumber.kitsune.data.presentation.model.user.User
import io.github.drumber.kitsune.data.repository.UserRepository
import io.github.drumber.kitsune.domain.auth.LogOutUserUseCase
import io.github.drumber.kitsune.util.logE
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ProfileViewModel(
    private val userRepository: UserRepository,
    private val logOutUser: LogOutUserUseCase
) : ViewModel() {

    private val _refreshTrigger = MutableSharedFlow<Unit>()

    private val _userModel = combine(
        userRepository.localUser,
        _refreshTrigger.onStart { emit(Unit) }
    ) { user, _ ->
            try {
                user?.let { fetchFullUserModel(it.id) ?: user.toUser() }
            } finally {
                _uiState.update {
                    it.copy(
                        isInitialLoading = false,
                        isRefreshing = false
                    )
                }
            }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(),
            initialValue = userRepository.localUser.value?.toUser()
        )

    val userModel = _userModel

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState = _uiState.asStateFlow()

    fun getUser(): User? {
        return _userModel.replayCache.firstOrNull() ?: userRepository.localUser.value?.toUser()
    }

    fun refreshUser() {
        viewModelScope.launch {
            _uiState.update { it.copy(isRefreshing = true) }
            _refreshTrigger.emit(Unit)
        }
    }

    private suspend fun fetchFullUserModel(userId: String): User? {
        return try {
            userRepository.fetchUser(userId, FULL_USER_FILTER)
                ?: throw NoDataException("Received data is null.")
        } catch (e: Exception) {
            logE("Failed to fetch full user model.", e)
            null
        }
    }

    suspend fun logOut() {
        viewModelScope.async {
            logOutUser()
        }.await()
    }

    companion object {
        val FULL_USER_FILTER
            get() = Filter()
                .include("stats", "favorites.item", "waifu", "profileLinks.profileLinkSite")
                .fields("media", *Defaults.MINIMUM_COLLECTION_FIELDS)
                .fields("characters", *Defaults.MINIMUM_CHARACTER_FIELDS)
    }
}

data class ProfileUiState(
    val isRefreshing: Boolean = false,
    val isInitialLoading: Boolean = true
)