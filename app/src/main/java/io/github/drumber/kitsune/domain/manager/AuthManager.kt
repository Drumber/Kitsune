package io.github.drumber.kitsune.domain.manager

import io.github.drumber.kitsune.domain.Result
import io.github.drumber.kitsune.domain.model.infrastructure.auth.AccessToken
import io.github.drumber.kitsune.domain.model.infrastructure.auth.ObtainAccessToken
import io.github.drumber.kitsune.domain.model.infrastructure.auth.RefreshAccessToken
import io.github.drumber.kitsune.domain.repository.AccessTokenRepository
import io.github.drumber.kitsune.domain.service.auth.AuthService
import io.github.drumber.kitsune.exception.AccessTokenNotRefreshedException
import io.github.drumber.kitsune.exception.AccessTokenObtainException
import io.github.drumber.kitsune.exception.AccessTokenRefreshException
import io.github.drumber.kitsune.util.logD
import io.github.drumber.kitsune.util.logE
import io.github.drumber.kitsune.util.logI
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import retrofit2.HttpException
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.seconds

class AuthManager(
    private val accessTokenRepository: AccessTokenRepository,
    private val service: AuthService,
) {

    companion object {
        val REFRESH_PRIOR_TIME = 1.days // refresh one day (in seconds) before expiring
    }

    private val mutex = Mutex()

    private val _logoutSignal = MutableSharedFlow<Unit>()
    val logOutSignal
        get() = _logoutSignal.asSharedFlow()

    fun getAccessToken() = accessTokenRepository.accessToken

    fun hasAccessToken(): Boolean {
        return accessTokenRepository.accessToken != null
    }

    /**
     * Get the timestamp in seconds on which the access token expires.
     */
    fun getAccessTokenExpirationTime(): Long? {
        val token = accessTokenRepository.accessToken ?: return null
        val tokenCreatedAt = token.createdAt ?: return null
        val tokenExpiresIn = token.expiresIn ?: return null

        return tokenCreatedAt + tokenExpiresIn
    }

    /**
     * Check if the access token is considered as expired.
     *
     * The access token is considered expired if the time specified with [REFRESH_PRIOR_TIME] has elapsed before the expiry date.
     */
    fun isAccessTokenConsideredExpired(): Boolean {
        val expirationTimeMillis = getAccessTokenExpirationTime()?.seconds ?: return false

        return System.currentTimeMillis() >= (expirationTimeMillis - REFRESH_PRIOR_TIME).inWholeMilliseconds
    }

    fun logout() {
        accessTokenRepository.clearAccessToken()
        logI("Successfully logged out.")
    }

    suspend fun login(username: String, password: String): Result<Unit> {
        val result = obtainAccessToken(ObtainAccessToken.build(username, password))

        return when (result) {
            is Result.Success -> {
                accessTokenRepository.setAccessToken(result.data)
                logD("Successfully logged in.")
                Result.Success(Unit)
            }

            is Result.Error -> Result.Error(
                AccessTokenObtainException(
                    "Failed to obtain access token.",
                    result.exception
                )
            )
        }
    }

    suspend fun refreshAccessToken(): Result<AccessToken> {
        val accessToken = accessTokenRepository.accessToken
            ?: return Result.Error(AccessTokenNotRefreshedException("Access token is null."))
        val refreshToken = accessToken.refreshToken
            ?: return Result.Error(AccessTokenNotRefreshedException("Refresh token is null."))

        return refreshAccessToken(refreshToken)
    }

    private suspend fun refreshAccessToken(refreshToken: String): Result<AccessToken> {
        mutex.withLock {
            logD("Refreshing access token...")
            val result = submitRefreshAccessToken(RefreshAccessToken(refreshToken = refreshToken))

            return when (result) {
                is Result.Success -> {
                    accessTokenRepository.setAccessToken(result.data)
                    logD("Successfully refreshed access token.")
                    result
                }

                is Result.Error -> {
                    triggerLogoutOnHttpError(result.exception)
                    return Result.Error(
                        AccessTokenRefreshException(
                            "Failed to refresh access token.",
                            result.exception
                        )
                    )
                }
            }
        }
    }

    private suspend fun triggerLogoutOnHttpError(exception: Exception) {
        if (exception is HttpException && exception.code() in 400..499) {
            _logoutSignal.emit(Unit)
        }
    }

    private suspend fun obtainAccessToken(obtainAccessToken: ObtainAccessToken): Result<AccessToken> {
        return try {
            val token = service.obtainAccessToken(obtainAccessToken)
            Result.Success(token)
        } catch (e: Exception) {
            logE("Error while obtaining access token.", e)
            if (e is HttpException) {
                logD("Error response body is: ${e.response()?.errorBody()?.string()}")
            }
            Result.Error(e)
        }
    }

    private suspend fun submitRefreshAccessToken(refreshAccessToken: RefreshAccessToken): Result<AccessToken> {
        return try {
            val token = service.refreshToken(refreshAccessToken)
            Result.Success(token)
        } catch (e: Exception) {
            logE("Error while refreshing access token.", e)
            if (e is HttpException) {
                logD("Error response body is: ${e.response()?.errorBody()?.string()}")
            }
            Result.Error(e)
        }
    }

}