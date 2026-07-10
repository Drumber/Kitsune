package io.github.drumber.kitsune.ui.profile

import androidx.lifecycle.ViewModel
import io.github.drumber.kitsune.data.presentation.model.user.User
import kotlinx.coroutines.flow.StateFlow

abstract class BaseProfileViewModel : ViewModel() {

    abstract val userModel: StateFlow<User?>
    abstract val uiState: StateFlow<ProfileUiState>

    fun getUser(): User? {
        return userModel.value
    }

    abstract fun refreshUser()
}

interface ProfileUiState {
    val isRefreshing: Boolean
    val isInitialLoading: Boolean
}
