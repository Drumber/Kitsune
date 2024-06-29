package io.github.drumber.kitsune.domain.auth

import io.github.drumber.kitsune.data.source.local.auth.LocalAccessToken

sealed interface RefreshResult {
    /** The access token was successfully refreshed. */
    data class Success(val accessToken: LocalAccessToken) : RefreshResult

    /** The access token could not be refreshed, e.g. invalid or expired refresh token. */
    data object Failure : RefreshResult

    /** Refresh failed due to an error. */
    data class Error(val exception: Exception) : RefreshResult
}