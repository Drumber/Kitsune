package io.github.drumber.kitsune.domain.user

import io.github.drumber.kitsune.data.repository.UserRepository
import io.github.drumber.kitsune.domain.auth.IsUserLoggedInUseCase
import io.github.drumber.kitsune.domain.auth.RefreshAccessTokenIfExpiredUseCase
import io.github.drumber.kitsune.domain.auth.RefreshResult
import io.github.drumber.kitsune.util.logI

class UpdateLocalUserUseCase(
    private val userRepository: UserRepository,
    private val isUserLoggedIn: IsUserLoggedInUseCase,
    private val refreshAccessTokenIfExpired: RefreshAccessTokenIfExpiredUseCase
) {

    suspend operator fun invoke() {
        if (!isUserLoggedIn()) {
            logI("Cannot update local user: User is not logged in.")
            return
        }

        val refreshResult = refreshAccessTokenIfExpired()
        if (refreshResult != null && refreshResult !is RefreshResult.Success) {
            logI("Cannot update local user: Access token refresh failed.")
            return
        }

        userRepository.fetchAndStoreLocalUserFromNetwork()
    }

}