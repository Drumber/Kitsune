package io.github.drumber.kitsune.domain.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import io.github.drumber.kitsune.constants.Defaults
import io.github.drumber.kitsune.domain.Result
import io.github.drumber.kitsune.domain.model.infrastructure.user.User
import io.github.drumber.kitsune.domain.service.Filter
import io.github.drumber.kitsune.domain.service.user.UserService
import io.github.drumber.kitsune.exception.AccessTokenRefreshException
import io.github.drumber.kitsune.exception.ReceivedDataException
import io.github.drumber.kitsune.preference.UserPreferences
import io.github.drumber.kitsune.util.logE
import io.github.drumber.kitsune.util.logI
import kotlin.properties.Delegates

class UserRepository(
    private val service: UserService,
    private val authRepository: AuthRepository,
    private val userPreferences: UserPreferences
) {

    var user: User? by Delegates.observable(null) { _, _, newValue ->
        _userLiveData.postValue(newValue)
    }
        private set

    private val _userLiveData = MutableLiveData<User?>()
    val userLiveData: LiveData<User?>
        get() = _userLiveData

    val hasUser: Boolean
        get() = user != null

    init {
        user = userPreferences.getStoredUserModel()
    }

    val userReLoginPrompt = MutableLiveData(false)

    fun logOut() {
        authRepository.logout()
        user = null
        userPreferences.clearUserModel()
    }

    private suspend fun requestUser(): Result<User> {
        val filter = Filter()
            .filter("self", "true")
            .include("waifu")
            .fields("characters", *Defaults.MINIMUM_CHARACTER_FIELDS)
        return try {
            val userModel = service.allUsers(filter.options).get()?.firstOrNull()
                ?: throw ReceivedDataException("Received invalid user data.")
            Result.Success(userModel)
        } catch (e: Exception) {
            logE("Error while obtaining user model.", e)
            Result.Error(e)
        }
    }

    suspend fun login(username: String, password: String): Result<User> {
        val accessResult = authRepository.login(username, password)

        if (accessResult is Result.Error) {
            return Result.Error(accessResult.exception)
        }

        val result = requestUser()

        if (result is Result.Success) {
            setUserModel(result.data)
        }

        return result
    }

    suspend fun updateUserCache() {
        val refreshResult = authRepository.refreshAccessTokenIfExpired()

        if (refreshResult is Result.Error && refreshResult.exception is AccessTokenRefreshException) {
            logI("Failed to refresh access token. Trigger log out...")
            // failed to automatically refresh access token; log out and notify user about re-login
            logOut()
            userReLoginPrompt.postValue(true)
            return
        }

        if (!authRepository.isLoggedIn) {
            logI("Cannot update user cache: No access token available.")
            return
        }

        val result = requestUser()

        if (result is Result.Success) {
            setUserModel(result.data)
        }
    }

    fun updateUserModel(updatedUser: User) {
        if (updatedUser.id.isNullOrBlank()) {
            throw IllegalArgumentException("User ID must not be null.")
        } else if (updatedUser.id != user?.id) {
            throw IllegalArgumentException("User ID is not equal to the cached user ID or the user is not logged in.")
        }
        setUserModel(updatedUser)
        logI("Updated cached user model.")
    }

    private fun setUserModel(user: User) {
        this.user = user
        userPreferences.storeUserModel(user)
    }

}