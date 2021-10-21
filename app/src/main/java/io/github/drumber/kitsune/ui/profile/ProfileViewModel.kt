package io.github.drumber.kitsune.ui.profile

import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import io.github.drumber.kitsune.data.model.auth.User
import io.github.drumber.kitsune.data.repository.UserRepository

class ProfileViewModel(userRepository: UserRepository): ViewModel() {

    val userModel: LiveData<User?> = Transformations.map(userRepository.userLiveData) { it }

}