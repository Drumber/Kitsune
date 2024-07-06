package io.github.drumber.kitsune.ui.settings

import android.content.Context
import androidx.annotation.StringRes
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.map
import androidx.lifecycle.viewModelScope
import io.github.drumber.kitsune.R
import io.github.drumber.kitsune.data.common.exception.NoDataException
import io.github.drumber.kitsune.data.repository.UserRepository
import io.github.drumber.kitsune.data.source.local.user.model.LocalUser
import io.github.drumber.kitsune.util.logE
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val userRepository: UserRepository
) : ViewModel() {

    val userModel = userRepository.localUser.asLiveData().map { it }

    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean>
        get() = _isLoading

    var errorMessageListener: ((ErrorMessage) -> Unit)? = null

    init {
        // make sure cached user data is up-to-date
        viewModelScope.launch(Dispatchers.IO) {
            try {
                userRepository.fetchAndStoreLocalUserFromNetwork()
            } catch (e: Exception) {
                logE("Failed to update local user model from network.", e)
            }
        }
    }

    fun updateUser(user: LocalUser) {
        _isLoading.value = true
        viewModelScope.launch(Dispatchers.IO) {
            try {
                userRepository.updateUser(user.id, user)
                    ?: throw NoDataException("Received user data is null.")
                userRepository.fetchAndStoreLocalUserFromNetwork()
            } catch (e: Exception) {
                logE("Failed to update user settings.", e)
                errorMessageListener?.invoke(ErrorMessage(R.string.error_user_update_failed))
                // workaround to trigger an update to reset preference values from the user model
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