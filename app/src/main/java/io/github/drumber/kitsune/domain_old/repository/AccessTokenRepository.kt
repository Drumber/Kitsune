package io.github.drumber.kitsune.domain_old.repository

import io.github.drumber.kitsune.domain_old.model.infrastructure.auth.AccessToken
import io.github.drumber.kitsune.preference.AuthPreference
import io.github.drumber.kitsune.util.logD

class AccessTokenRepository(
    private val authPreferences: AuthPreference
) {

    // in-memory cache of the accessToken object
    var accessToken: AccessToken? = null
        private set

    init {
//        accessToken = authPreferences.loadAccessToken()
        logD(if (accessToken == null) "No access token stored." else "Loaded stored access token.")
    }

    fun clearAccessToken() {
        accessToken = null
        authPreferences.clearAccessToken()
        logD("Cleared access token.")
    }

    fun setAccessToken(accessToken: AccessToken) {
        this.accessToken = accessToken
//        authPreferences.storeAccessToken(accessToken)
        logD("Set access token.")
    }

}