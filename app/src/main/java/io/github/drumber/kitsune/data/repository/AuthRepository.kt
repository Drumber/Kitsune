package io.github.drumber.kitsune.data.repository

import io.github.drumber.kitsune.data.Result
import io.github.drumber.kitsune.data.model.auth.AccessToken
import io.github.drumber.kitsune.data.service.auth.AuthService
import io.github.drumber.kitsune.exception.AccessTokenRefreshException

class AuthRepository(val service: AuthService) {

    // in-memory cache of the accessToken object
    var accessToken: AccessToken? = null
        private set

    val isLoggedIn: Boolean
        get() = accessToken != null

    val isAccessTokenExpired: Boolean
        get() = accessToken?.expiresIn?.let {
            System.currentTimeMillis() >= (it * 1000L)
        } ?: false

    init {
        // TODO: load stored access token
        accessToken = null
    }

    fun logout() {
        accessToken = null
    }

    suspend fun login(username: String, password: String): Result<AccessToken> {
        val result = try {
            val token = service.obtainAccessToken(username = username, password = password)
            Result.Success(token)
        } catch (e: Exception) {
            Result.Error(e)
        }

        if (result is Result.Success) {
            setLoggedInToken(result.data)
        }

        return result
    }

    suspend fun refreshAccessToken(refreshToken: String): Result<AccessToken> {
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
        if (isAccessTokenExpired) {
            accessToken?.refreshToken?.let {
                return refreshAccessToken(it)
            }
        }
        return Result.Error(AccessTokenRefreshException("Access token is not expired or token is null."))
    }

    private fun setLoggedInToken(token: AccessToken) {
        this.accessToken = token
        // If user credentials will be cached in local storage, it is recommended it be encrypted
        // @see https://developer.android.com/training/articles/keystore
    }
}