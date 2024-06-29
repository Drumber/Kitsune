package io.github.drumber.kitsune.domain.auth

import io.github.drumber.kitsune.data.repository.AccessTokenRepository

class IsUserLoggedInUseCase(
    private val accessTokenRepository: AccessTokenRepository
) {

    operator fun invoke() = accessTokenRepository.hasAccessToken()

}