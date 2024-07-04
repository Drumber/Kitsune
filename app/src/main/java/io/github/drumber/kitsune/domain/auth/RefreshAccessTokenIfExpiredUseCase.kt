package io.github.drumber.kitsune.domain.auth

import io.github.drumber.kitsune.data.repository.AccessTokenRepository
import io.github.drumber.kitsune.data.source.local.auth.model.LocalAccessToken
import io.github.drumber.kitsune.util.logI
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.seconds

class RefreshAccessTokenIfExpiredUseCase(
    private val accessTokenRepository: AccessTokenRepository,
    private val refreshAccessToken: RefreshAccessTokenUseCase
) {

    companion object {
        val REFRESH_PRIOR_TIME = 1.days // refresh access token one day before it expires
    }

    suspend operator fun invoke(): RefreshResult? {
        val accessToken = accessTokenRepository.getAccessToken() ?: return null

        if (accessToken.isAccessTokenConsideredExpired()) {
            logI("Refresh: Access token is considered expired.")
            return refreshAccessToken()
        }
        return null
    }

    /**
     * Check if the access token is considered as expired.
     *
     * The access token is considered expired if the time specified with [REFRESH_PRIOR_TIME] has elapsed before the expiry date.
     */
    private fun LocalAccessToken.isAccessTokenConsideredExpired(): Boolean {
        val expirationTime = getExpirationTimeInSeconds().seconds
        return System.currentTimeMillis() >= (expirationTime - REFRESH_PRIOR_TIME).inWholeMilliseconds
    }

}