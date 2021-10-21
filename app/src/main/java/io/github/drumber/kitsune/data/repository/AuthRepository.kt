package io.github.drumber.kitsune.data.repository

import io.github.drumber.kitsune.data.Result
import io.github.drumber.kitsune.data.model.auth.AccessToken
import io.github.drumber.kitsune.data.service.auth.AuthService
import io.github.drumber.kitsune.exception.AccessTokenRefreshException
import io.github.drumber.kitsune.preference.AuthPreferences
import io.github.drumber.kitsune.util.logD
import io.github.drumber.kitsune.util.logE

class AuthRepository(
    private val service: AuthService,
    private val authPreferences: AuthPreferences
) {

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
                    System.currentTimeMillis() >= (expireDate * 1000L)
                }
            }
        } ?: false

    init {
        accessToken = authPreferences.getStoredAccessToken()
        logD(if (accessToken == null) "No access token stored." else "Loaded stored access token.")
    }

    fun logout() {
        accessToken = null
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
        }

        return result
    }

    suspend fun refreshAccessToken(refreshToken: String): Result<AccessToken> {
        logD("Refreshing access token...")
        val result = try {
            val token = service.refreshToken(refreshToken = refreshToken)
            Result.Success(token)
        } catch (e: Exception) {
            Result.Error(e)
        }

        if (result is Result.Success) {
            setLoggedInToken(result.data)
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
        return Result.Error(AccessTokenRefreshException("Access token is not expired or token is null."))
    }

    private fun setLoggedInToken(token: AccessToken) {
        this.accessToken = token
        authPreferences.storeAccessToken(token)
    }
}