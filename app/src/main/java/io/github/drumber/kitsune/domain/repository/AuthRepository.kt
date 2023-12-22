package io.github.drumber.kitsune.domain.repository

import io.github.drumber.kitsune.domain.Result
import io.github.drumber.kitsune.domain.model.infrastructure.auth.AccessToken
import io.github.drumber.kitsune.domain.service.auth.AuthService
import io.github.drumber.kitsune.exception.AccessTokenNotRefreshedException
import io.github.drumber.kitsune.exception.AccessTokenRefreshException
import io.github.drumber.kitsune.preference.AuthPreferences
import io.github.drumber.kitsune.util.logD
import io.github.drumber.kitsune.util.logE
import kotlinx.coroutines.sync.Mutex
import retrofit2.HttpException

class AuthRepository(
    private val service: AuthService,
    private val authPreferences: AuthPreferences
) {

    companion object {
        const val REFRESH_PRIOR_TIME = 60*60*24 // refresh one day (in seconds) before expiring
    }

    // in-memory cache of the accessToken object
    var accessToken: AccessToken? = null
        private set

    val isLoggedIn: Boolean
        get() = accessToken != null

    val isAccessTokenExpired: Boolean
        get() = accessToken?.let { token ->
            token.createdAt?.let { created ->
                token.expiresIn?.let { expires ->
                    // date in seconds when the token expires
                    val expireDate = created + expires
                    logD("Access token expires on $expireDate (in seconds)")
                    System.currentTimeMillis() >= (expireDate * 1000L) - REFRESH_PRIOR_TIME
                }
            }
        } ?: false

    private val mutex = Mutex()

    init {
        accessToken = authPreferences.getStoredAccessToken()
        logD(if (accessToken == null) "No access token stored." else "Loaded stored access token.")
    }

    fun logout() {
        accessToken = null
        authPreferences.clearAccessToken()
    }

    suspend fun login(username: String, password: String): Result<AccessToken> {
        val result = try {
            val token = service.obtainAccessToken(username = username, password = password)
            Result.Success(token)
        } catch (e: Exception) {
            logE("Error while obtaining authentication token.", e)
            Result.Error(e)
        }

        if (result is Result.Success) {
            setLoggedInToken(result.data)
            logD("Successfully logged in.")
        }

        return result
    }

    suspend fun refreshAccessToken(refreshToken: String): Result<AccessToken> {
        logD("Refreshing access token...")
        val result = try {
            mutex.lock()
            val token = service.refreshToken(refreshToken = refreshToken)
            Result.Success(token)
        } catch (e: Exception) {
            logE("Error while refreshing access token.", e)
            if (e is HttpException) {
                logD("Error response body is: ${e.response()?.errorBody()?.string()}")
            }
            Result.Error(e)
        } finally {
            mutex.unlock()
        }

        if (result is Result.Success) {
            setLoggedInToken(result.data)
            logD("Successfully refreshed access token.")
        } else if (result is Result.Error) {
            return Result.Error(AccessTokenRefreshException("Failed to refresh access token.", result.exception))
        }

        return result
    }

    suspend fun refreshAccessTokenIfExpired(): Result<AccessToken> {
        if (isLoggedIn && isAccessTokenExpired) {
            accessToken?.refreshToken?.let {
                return refreshAccessToken(it)
            }
        }

        logD("Did not refresh access token since it is null or not expired.")
        return Result.Error(AccessTokenNotRefreshedException("Access token is not expired or token is null."))
    }

    private fun setLoggedInToken(token: AccessToken) {
        this.accessToken = token
        authPreferences.storeAccessToken(token)
    }
}