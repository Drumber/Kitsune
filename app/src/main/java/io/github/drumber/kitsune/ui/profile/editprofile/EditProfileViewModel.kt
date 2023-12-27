package io.github.drumber.kitsune.ui.profile.editprofile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.jasminb.jsonapi.JSONAPIDocument
import io.github.drumber.kitsune.domain.model.infrastructure.user.User
import io.github.drumber.kitsune.domain.repository.UserRepository
import io.github.drumber.kitsune.domain.service.user.UserService
import io.github.drumber.kitsune.exception.ReceivedDataException
import io.github.drumber.kitsune.util.logE
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class EditProfileViewModel(
    private val userRepository: UserRepository,
    private val service: UserService
) : ViewModel() {

    val loadingStateFlow: StateFlow<LoadingState>

    val profileStateFlow: StateFlow<ProfileState>
    val canUpdateProfileFlow: Flow<Boolean>

    val profileState
        get() = profileStateFlow.value

    val acceptChanges: (ProfileState) -> Unit

    private val acceptLoadingState: (LoadingState) -> Unit

    init {
        val user = userRepository.user

        val initialProfileState = ProfileState(
            location = user?.location ?: "",
            birthday = user?.birthday ?: "",
            gender = user?.getGenderWithoutCustomGender() ?: "",
            customGender = user?.getCustomGenderOrNull() ?: "",
            waifuOrHusbando = user?.waifuOrHusbando ?: "",
            about = user?.about ?: ""
        )

        val _profileStateFlow = MutableSharedFlow<ProfileState>()
        profileStateFlow = _profileStateFlow
            .distinctUntilChanged()
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(stopTimeoutMillis = 5000),
                initialValue = initialProfileState
            )
        canUpdateProfileFlow = profileStateFlow.map { it != initialProfileState }

        acceptChanges = { changes ->
            viewModelScope.launch { _profileStateFlow.emit(changes) }
        }

        val _loadingStateFlow = MutableSharedFlow<LoadingState>()
        loadingStateFlow = _loadingStateFlow
            .distinctUntilChanged()
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(stopTimeoutMillis = 5000),
                initialValue = LoadingState.NotLoading
            )
        acceptLoadingState = { loadingState ->
            viewModelScope.launch { _loadingStateFlow.emit(loadingState) }
        }
    }

    fun hasUser() = userRepository.hasUser

    fun updateUserProfile() {
        val user = userRepository.user ?: return
        val changes = profileState
        val updatedUserModel = User(
            id = user.id,
            location = changes.location,
            birthday = changes.birthday,
            gender = if (changes.gender == "custom") changes.customGender else changes.gender,
            waifuOrHusbando = changes.waifuOrHusbando,
            about = changes.about
        )

        if (user.id.isNullOrBlank()) return

        acceptLoadingState(LoadingState.Loading)
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val response = service.updateUser(user.id, JSONAPIDocument(updatedUserModel))

                if (response.get() != null) {
                    userRepository.updateUserModel(updatedUserModel)
                    acceptLoadingState(LoadingState.Success)
                } else {
                    throw ReceivedDataException("Received user data is null.")
                }
            } catch (e: Exception) {
                logE("Failed to update user profile.", e)
                acceptLoadingState(LoadingState.Error(e))
            }
        }
    }

    private fun User.getGenderWithoutCustomGender(): String? {
        return when (gender) {
            null, "", "male", "female", "secret" -> gender
            else -> "custom"
        }
    }

    private fun User.getCustomGenderOrNull(): String? {
        return when (gender) {
            "male", "female", "secret" -> null
            else -> gender
        }
    }
}

data class ProfileState(
    val location: String,
    val birthday: String,
    val gender: String,
    val customGender: String,
    val waifuOrHusbando: String,
    val about: String
)

sealed class LoadingState {
    data object NotLoading : LoadingState()
    data object Loading : LoadingState()
    data object Success : LoadingState()
    data class Error(val exception: Exception) : LoadingState()
}
