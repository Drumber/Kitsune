package io.github.drumber.kitsune.domain.auth

import io.github.drumber.kitsune.data.repository.AccessTokenRepository
import io.github.drumber.kitsune.data.repository.UserRepository
import io.github.drumber.kitsune.shared.logE
import io.github.drumber.kitsune.shared.logI
import retrofit2.HttpException

class RefreshAccessTokenUseCase(
    private val accessTokenRepository: AccessTokenRepository,
    private val userRepository: UserRepository,
    private val logOutUser: LogOutUserUseCase
) {

    suspend operator fun invoke(): RefreshResult {
        logI("Refresh: Refreshing access token.")
        val accessToken = try {
            accessTokenRepository.refreshAccessToken()
        } catch (e: HttpException) {
            // trigger logout if the refresh token is invalid
            if (e.code() in 400..499) {
                logE("Refresh: Failed with status code ${e.code()}. Triggering logout...", e)
                triggerLogOutWithLoginPrompt()
                return RefreshResult.Failure
            }

            logE("Refresh: Failed to refresh access token.", e)
            return RefreshResult.Error(e)
        } catch (e: Exception) {
            logE("Refresh: Failed to refresh access token.", e)
            return RefreshResult.Error(e)
        }

        logI("Refresh: Successfully refreshed access token.")
        return RefreshResult.Success(accessToken)
    }

    /**
     * Log out the current user and prompt for re-login.
     */
    private suspend fun triggerLogOutWithLoginPrompt() {
        logOutUser()
        userRepository.promptUserReLogIn()
    }

}