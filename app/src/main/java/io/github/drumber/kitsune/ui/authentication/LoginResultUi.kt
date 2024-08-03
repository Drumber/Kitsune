package io.github.drumber.kitsune.ui.authentication

/**
 * Authentication result : success (user details) or error message.
 */
data class LoginResultUi(
    val success: LoggedInUserView? = null,
    val error: Int? = null
)