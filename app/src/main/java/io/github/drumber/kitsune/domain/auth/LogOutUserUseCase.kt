package io.github.drumber.kitsune.domain.auth

import io.github.drumber.kitsune.data.repository.AccessTokenRepository
import io.github.drumber.kitsune.data.repository.UserRepository
import io.github.drumber.kitsune.util.logI

class LogOutUserUseCase(
    private val userRepository: UserRepository,
    private val accessTokenRepository: AccessTokenRepository
) {

    suspend operator fun invoke() {
        logI("Logout: Clearing access token and local user.")
        accessTokenRepository.clearAccessToken()
        userRepository.clearLocalUser()
    }

}