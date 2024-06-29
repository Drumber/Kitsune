package io.github.drumber.kitsune.domain.auth

import io.github.drumber.kitsune.data.repository.AccessTokenRepository
import io.github.drumber.kitsune.data.repository.UserRepository
import io.github.drumber.kitsune.util.logE
import io.github.drumber.kitsune.util.logI
import retrofit2.HttpException

class LogInUserUseCase(
    private val userRepository: UserRepository,
    private val accessTokenRepository: AccessTokenRepository,
    private val isUserLoggedIn: IsUserLoggedInUseCase
) {

    suspend operator fun invoke(username: String, password: String): LoginResult {
        if (isUserLoggedIn()) {
            logI("Login: Did not log in because the user is already logged in.")
            return LoginResult.AlreadyLoggedIn
        }

        logI("Login: Obtaining access token.")
        val accessToken = try {
            accessTokenRepository.obtainAccessToken(username, password)
        } catch (e: HttpException) {
            logE("Login: Failed to obtain access token.", e)
            return when (e.code()) {
                400 -> LoginResult.Failure
                else -> LoginResult.Error(e)
            }
        } catch (e: Exception) {
            logE("Login: Failed to obtain access token.", e)
            return LoginResult.Error(e)
        }

        val localUser = try {
            userRepository.updateLocalUserFromNetwork()
            userRepository.localUser.value
        } catch (e: Exception) {
            logE("Login: Failed to update local user from network.", e)
            null
        }

        logI("Login: Successfully logged in.")
        return LoginResult.Success(accessToken, localUser)
    }

}