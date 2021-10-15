package io.github.drumber.kitsune.ui.profile

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import io.github.drumber.kitsune.data.model.auth.User
import io.github.drumber.kitsune.data.repository.UserRepository

class ProfileViewModel(private val userRepository: UserRepository): ViewModel() {

    private val _userModel = MutableLiveData(userRepository.user)

    val userModel: LiveData<User?>
        get() = _userModel

    fun updateUserModel() {
        if(userRepository.user != userModel.value) {
            _userModel.value = userRepository.user
        }
    }

}