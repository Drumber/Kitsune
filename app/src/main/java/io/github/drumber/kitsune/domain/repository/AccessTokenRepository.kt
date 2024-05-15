package io.github.drumber.kitsune.domain.repository

import io.github.drumber.kitsune.domain.model.infrastructure.auth.AccessToken
import io.github.drumber.kitsune.preference.AuthPreferences
import io.github.drumber.kitsune.util.logD

class AccessTokenRepository(
    private val authPreferences: AuthPreferences
) {

    // in-memory cache of the accessToken object
    var accessToken: AccessToken? = null
        private set

    init {
        accessToken = authPreferences.getStoredAccessToken()
        logD(if (accessToken == null) "No access token stored." else "Loaded stored access token.")
    }

    fun clearAccessToken() {
        accessToken = null
        authPreferences.clearAccessToken()
        logD("Cleared access token.")
    }

    fun setAccessToken(accessToken: AccessToken) {
        this.accessToken = accessToken
        authPreferences.storeAccessToken(accessToken)
        logD("Set access token.")
    }

}