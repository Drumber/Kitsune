package io.github.drumber.kitsune.ui.main

import androidx.annotation.IdRes
import androidx.lifecycle.ViewModel
import io.github.drumber.kitsune.R
import io.github.drumber.kitsune.data.repository.AccessTokenRepository
import io.github.drumber.kitsune.data.repository.AccessTokenRepository.AccessTokenState
import io.github.drumber.kitsune.data.repository.UserRepository
import kotlinx.coroutines.flow.map

class MainActivityViewModel(
    userRepository: UserRepository,
    private val accessTokenRepository: AccessTokenRepository
) : ViewModel() {

    /** Destination ID of the current selected bottom navigation item. */
    @IdRes
    var currentNavRootDestId: Int = R.id.main_fragment

    val reLoginPrompt = userRepository.userReLogInPrompt

    val localUser = userRepository.localUser

    val isLoggedInFlow =
        accessTokenRepository.accessTokenState.map { it == AccessTokenState.PRESENT }

    fun isLoggedIn() = accessTokenRepository.accessTokenState.value == AccessTokenState.PRESENT
}