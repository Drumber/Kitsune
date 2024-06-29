package io.github.drumber.kitsune.ui.profile

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.liveData
import androidx.lifecycle.switchMap
import androidx.lifecycle.viewModelScope
import io.github.drumber.kitsune.constants.Defaults
import io.github.drumber.kitsune.data.repository.UserRepository
import io.github.drumber.kitsune.data.source.local.user.model.LocalUser
import io.github.drumber.kitsune.domain.auth.LogOutUserUseCase
import io.github.drumber.kitsune.domain_old.service.Filter
import io.github.drumber.kitsune.exception.ReceivedDataException
import io.github.drumber.kitsune.util.logE
import io.github.drumber.kitsune.util.network.ResponseData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ProfileViewModel(
    private val userRepository: UserRepository,
    private val logOutUser: LogOutUserUseCase
) : ViewModel() {

    // simple user model stored in user preference
    val userModel: LiveData<LocalUser?> = userRepository.localUser.asLiveData()

    // full user model including stats and favorites
    val fullUserModel: LiveData<ResponseData<LocalUser>> = userModel.switchMap {
        it?.id?.let { userId ->
            liveData(context = Dispatchers.IO) {
                val response = try {
                    val fullUser = userRepository.getUser(userId, FULL_USER_FILTER)
                        ?: throw ReceivedDataException("Received data is null.")
                    ResponseData.Success(fullUser)
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
                userRepository.updateLocalUserFromNetwork()
            } catch (e: Exception) {
                logE("Failed to refresh user model.", e)
            } finally {
                _isLoading.postValue(false)
            }
        }
    }

    fun logOut() {
        logOutUser()
    }

    companion object {
        val FULL_USER_FILTER
            get() = Filter()
                .include("stats", "favorites.item", "waifu", "profileLinks.profileLinkSite")
                .fields("media", *Defaults.MINIMUM_COLLECTION_FIELDS)
                .fields("characters", *Defaults.MINIMUM_CHARACTER_FIELDS)
    }

}