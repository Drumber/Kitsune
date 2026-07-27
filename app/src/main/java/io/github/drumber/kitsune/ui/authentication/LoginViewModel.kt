package io.github.drumber.kitsune.ui.authentication

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.drumber.kitsune.R
import io.github.drumber.kitsune.domain.auth.LogInUserUseCase
import io.github.drumber.kitsune.domain.auth.LoginResult
import io.github.drumber.kitsune.util.logE
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class LoginUiState(
    val username: String = "",
    val password: String = "",
    val usernameError: Int? = null,
    val passwordError: Int? = null,
    val isLoggingIn: Boolean = false
) {
    val isDataValid: Boolean
        get() = usernameError == null && username.isNotBlank() && password.isNotBlank()
}

class LoginViewModel(private val logInUser: LogInUserUseCase) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    private val _loginResult = MutableSharedFlow<LoginResultUi>(extraBufferCapacity = 1)
    val loginResult: SharedFlow<LoginResultUi> = _loginResult.asSharedFlow()

    fun setUsername(username: String) {
        _uiState.update {
            it.copy(
                username = username,
                usernameError = if (isUserNameValid(username)) null else R.string.invalid_username,
                passwordError = null
            )
        }
    }

    fun setPassword(password: String) {
        _uiState.update { it.copy(password = password, passwordError = null) }
    }

    fun login() {
        val state = _uiState.value
        if (!state.isDataValid || state.isLoggingIn) return

        _uiState.update { it.copy(isLoggingIn = true) }
        viewModelScope.launch(Dispatchers.IO) {
            val result = logInUser(state.username, state.password)

            if (result is LoginResult.Error) {
                logE("Failed to login to Kitsu.", result.exception)
            }

            withContext(Dispatchers.Main) {
                if (result is LoginResult.Success) {
                    _loginResult.tryEmit(
                        LoginResultUi(
                            success = LoggedInUserView(
                                displayName = result.localUser?.name ?: "Unknown"
                            )
                        )
                    )
                } else {
                    _uiState.update { it.copy(passwordError = R.string.login_failed) }
                    _loginResult.tryEmit(LoginResultUi(error = R.string.login_failed))
                }
                _uiState.update { it.copy(isLoggingIn = false) }
            }
        }
    }

    /**
     * Kitsu accepts either an email address or a username for login, so just verify the input
     * is non-blank and contains no whitespace.
     */
    private fun isUserNameValid(username: String) =
        username.isBlank() || username.none { it.isWhitespace() }
}
