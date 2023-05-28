package io.github.drumber.kitsune.ui.settings

import android.content.Context
import androidx.annotation.StringRes
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.map
import androidx.lifecycle.viewModelScope
import com.github.jasminb.jsonapi.JSONAPIDocument
import io.github.drumber.kitsune.R
import io.github.drumber.kitsune.data.model.user.User
import io.github.drumber.kitsune.data.repository.UserRepository
import io.github.drumber.kitsune.data.service.user.UserService
import io.github.drumber.kitsune.exception.ReceivedDataException
import io.github.drumber.kitsune.util.logE
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val userRepository: UserRepository,
    private val userService: UserService
) : ViewModel() {

    val userModel = userRepository.userLiveData.map { it }

    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean>
        get() = _isLoading

    var errorMessageListener: ((ErrorMessage) -> Unit)? = null

    init {
        // make sure cached user data is up-to-date
        viewModelScope.launch(Dispatchers.IO) {
            userRepository.updateUserCache()
        }
    }

    fun updateUser(user: User) {
        if (user.id.isNullOrBlank()) {
            errorMessageListener?.invoke(ErrorMessage(R.string.error_invalid_user))
            return
        }
        _isLoading.value = true
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val response = userService.updateUser(user.id, JSONAPIDocument(user))
                response.get()?.let { updatedUser ->
                    userRepository.updateUserModel(updatedUser)
                } ?: throw ReceivedDataException("Received user data is null.")
            } catch (e: Exception) {
                logE("Failed to update user settings.", e)
                errorMessageListener?.invoke(ErrorMessage(R.string.error_user_update_failed))
                // trigger to reset preference values from the user model
                (userModel as MutableLiveData).postValue(userModel.value)
            } finally {
                _isLoading.postValue(false)
            }
        }
    }


    data class ErrorMessage(
        @StringRes val stringRes: Int? = null,
        val message: String = ""
    ) {
        fun getMessage(context: Context) = if (stringRes != null) {
            context.getString(stringRes)
        } else {
            message
        }
    }

}