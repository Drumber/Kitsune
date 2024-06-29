package io.github.drumber.kitsune.domain.auth

import io.github.drumber.kitsune.data.source.local.auth.LocalAccessToken
import io.github.drumber.kitsune.data.source.local.user.model.LocalUser

sealed interface LoginResult {
    /** Successfully logged in */
    data class Success(val accessToken: LocalAccessToken, val localUser: LocalUser?) : LoginResult

    /** Login failed, e.g. wrong credentials. */
    data object Failure : LoginResult

    /** User is already logged in */
    data object AlreadyLoggedIn : LoginResult

    /** Login failed due to an error. */
    data class Error(val exception: Exception) : LoginResult
}