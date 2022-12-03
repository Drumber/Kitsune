package io.github.drumber.kitsune.ui.authentication

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.drumber.kitsune.R
import io.github.drumber.kitsune.data.Result
import io.github.drumber.kitsune.data.repository.UserRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class LoginViewModel(private val userRepository: UserRepository) : ViewModel() {

    private val _loginForm = MutableLiveData<LoginFormState>()
    val loginFormState: LiveData<LoginFormState> = _loginForm

    private val _loginResult = MutableLiveData<LoginResult>()
    val loginResult: LiveData<LoginResult> = _loginResult

    private val _isLoggingIn = MutableLiveData<Boolean>()
    val isLoggingIn: LiveData<Boolean> = _isLoggingIn

    fun login(username: String, password: String) {
        _isLoggingIn.value = true
        viewModelScope.launch(Dispatchers.IO) {
            val result = userRepository.login(username, password)

            if(result is Result.Error) {
                result.exception.printStackTrace()
            }

            withContext(Dispatchers.Main) {
                if (result is Result.Success) {
                    _loginResult.value =
                        LoginResult(success = LoggedInUserView(displayName = result.data.name ?: "Unknown"))

                } else {
                    _loginResult.value = LoginResult(error = R.string.login_failed)
                }
                _isLoggingIn.value = false
            }
        }
    }

    fun loginDataChanged(username: String, password: String) {
        if (!isUserNameValid(username)) {
            _loginForm.value = LoginFormState(usernameError = R.string.invalid_username)
        } else if (isPasswordValid(password)) {
            _loginForm.value = LoginFormState(isDataValid = true)
        }
    }

    private fun isUserNameValid(username: String): Boolean {
        // verify that username is an email address
        return ("""^\S+@\S+\.\S+$""".toRegex().matches(username))
    }

    private fun isPasswordValid(password: String): Boolean {
        return password.isNotBlank()
    }
}