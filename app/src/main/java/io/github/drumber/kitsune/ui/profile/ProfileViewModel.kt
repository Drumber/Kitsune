package io.github.drumber.kitsune.ui.profile

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.distinctUntilChanged
import androidx.lifecycle.liveData
import androidx.lifecycle.switchMap
import androidx.lifecycle.viewModelScope
import io.github.drumber.kitsune.constants.Defaults
import io.github.drumber.kitsune.domain.model.infrastructure.user.User
import io.github.drumber.kitsune.domain.repository.UserRepository
import io.github.drumber.kitsune.domain.service.Filter
import io.github.drumber.kitsune.domain.service.user.UserService
import io.github.drumber.kitsune.exception.ReceivedDataException
import io.github.drumber.kitsune.util.logE
import io.github.drumber.kitsune.util.network.ResponseData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ProfileViewModel(
    private val userRepository: UserRepository,
    userService: UserService
) : ViewModel() {

    // simple user model stored in user preference
    val userModel: LiveData<User?> = userRepository.userLiveData

    // full user model including stats and favorites
    val fullUserModel: LiveData<ResponseData<User>> = userModel.distinctUntilChanged().switchMap {
        it?.id?.let { userId ->
            liveData(context = Dispatchers.IO) {
                val response = try {
                    val response = userService.getUser(userId, FULL_USER_FILTER.options)
                    response.get()?.let { fullUser ->
                        ResponseData.Success(fullUser)
                    } ?: throw ReceivedDataException("Received data is null.")
                } catch (e: Exception) {
                    logE("Failed to fetch full user model.", e)
                    ResponseData.Error(e)
                }
                emit(response)
            }
        } ?: MutableLiveData(ResponseData.Error(ReceivedDataException("User is null.")))
    }

    private val _isLoading = MutableLiveData(false)
    val isLoading get() = _isLoading as LiveData<Boolean>

    fun refreshUser() {
        _isLoading.value = true
        viewModelScope.launch(Dispatchers.IO) {
            try {
                userRepository.updateUserCache()
            } catch (e: Exception) {
                logE("Failed to refresh user model.", e)
            } finally {
                _isLoading.postValue(false)
            }
        }
    }

    fun logOut() {
        userRepository.logOut()
    }

    companion object {
        val FULL_USER_FILTER
            get() = Filter()
                .include("stats", "favorites.item", "waifu")
                .fields("media", *Defaults.MINIMUM_COLLECTION_FIELDS)
                .fields("characters", *Defaults.MINIMUM_CHARACTER_FIELDS)
    }

}