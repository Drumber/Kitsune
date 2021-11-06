package io.github.drumber.kitsune.ui.profile

import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import io.github.drumber.kitsune.data.model.auth.User
import io.github.drumber.kitsune.data.repository.UserRepository
import io.github.drumber.kitsune.data.service.Filter
import io.github.drumber.kitsune.data.service.user.UserService
import io.github.drumber.kitsune.exception.ReceivedDataException
import io.github.drumber.kitsune.util.logE
import io.github.drumber.kitsune.util.network.ResponseData
import kotlinx.coroutines.Dispatchers

class ProfileViewModel(
    userRepository: UserRepository,
    userService: UserService
) : ViewModel() {

    // simple user model stored in user preference
    val userModel: LiveData<User?> = Transformations.map(userRepository.userLiveData) { it }

    // full user model including stats
    val fullUserModel: LiveData<ResponseData<User>> = Transformations.switchMap(userModel) {
        it?.id?.let { userId ->
            liveData(context = Dispatchers.IO) {
                val response = try {
                    val response = userService.getUser(userId, FULL_USER_FILTER.options)
                    response.get()?.let {
                            fullUser -> ResponseData.Success(fullUser)
                    } ?: throw ReceivedDataException("Received data is null.")
                } catch (e: Exception) {
                    logE("Failed to fetch full user model.", e)
                    ResponseData.Error(e)
                }
                emit(response)
            }
        }
    }

    companion object {
        val FULL_USER_FILTER
            get() = Filter().include("stats")
    }

}