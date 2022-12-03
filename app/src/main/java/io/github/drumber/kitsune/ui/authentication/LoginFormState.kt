package io.github.drumber.kitsune.ui.authentication

/**
 * Data validation state of the login form.
 */
data class LoginFormState(
    val usernameError: Int? = null,
    val isDataValid: Boolean = false
)