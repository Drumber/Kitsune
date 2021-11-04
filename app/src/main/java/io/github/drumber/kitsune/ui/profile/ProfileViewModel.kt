package io.github.drumber.kitsune.ui.profile

import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import io.github.drumber.kitsune.data.model.auth.User
import io.github.drumber.kitsune.data.repository.UserRepository
import io.github.drumber.kitsune.data.service.Filter
import io.github.drumber.kitsune.data.service.user.UserService
import io.github.drumber.kitsune.util.logE
import kotlinx.coroutines.Dispatchers

class ProfileViewModel(
    userRepository: UserRepository,
    userService: UserService
) : ViewModel() {

    // simple user model stored in user preference
    val userModel: LiveData<User?> = Transformations.map(userRepository.userLiveData) { it }

    // full user model including stats
    val fullUserModel: LiveData<User> = Transformations.switchMap(userModel) {
        it?.id?.let { userId ->
            liveData(context = Dispatchers.IO) {
                try {
                    val response = userService.getUser(userId, FULL_USER_FILTER.options)
                    response.get()?.let { fullUser -> emit(fullUser) }
                } catch (e: Exception) {
                    logE("Failed to fetch full user model.", e)
                }
            }
        }
    }

    companion object {
        val FULL_USER_FILTER
            get() = Filter().include("stats")
    }

}