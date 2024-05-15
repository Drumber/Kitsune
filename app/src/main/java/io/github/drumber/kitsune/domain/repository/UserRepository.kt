package io.github.drumber.kitsune.domain.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import io.github.drumber.kitsune.constants.Defaults
import io.github.drumber.kitsune.domain.Result
import io.github.drumber.kitsune.domain.manager.AuthManager
import io.github.drumber.kitsune.domain.model.infrastructure.auth.AccessToken
import io.github.drumber.kitsune.domain.model.infrastructure.user.User
import io.github.drumber.kitsune.domain.service.Filter
import io.github.drumber.kitsune.domain.service.user.UserService
import io.github.drumber.kitsune.exception.ReceivedDataException
import io.github.drumber.kitsune.preference.UserPreferences
import io.github.drumber.kitsune.util.logD
import io.github.drumber.kitsune.util.logE
import io.github.drumber.kitsune.util.logI
import kotlinx.coroutines.flow.onEach
import kotlin.properties.Delegates

class UserRepository(
    private val service: UserService,
    private val authManager: AuthManager,
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

    val userReLoginSignal = authManager.logOutSignal.onEach {
        logI("Failed to refresh access token. Trigger log out...")
        logOut()
    }

    fun logOut() {
        authManager.logout()
        user = null
        userPreferences.clearUserModel()
        logI("Successfully logged out.")
    }

    suspend fun login(username: String, password: String): Result<User> {
        val accessTokenResult = authManager.login(username, password)

        if (accessTokenResult is Result.Error) {
            return Result.Error(accessTokenResult.exception)
        }

        val result = requestUser()

        if (result is Result.Success) {
            setUserModel(result.data)
        }

        return result
    }

    suspend fun updateUserCache() {
        logD("Updating user cache.")
        if (authManager.isAccessTokenConsideredExpired()) {
            logD("Access token is expired and must be refreshed.")
            val refreshResult = refreshAccessToken()

            if (refreshResult is Result.Error) {
                logI("Cannot update user cache: Access token refresh failed.")
                return
            }
        }

        if (!authManager.hasAccessToken()) {
            logI("Cannot update user cache: No access token available.")
            return
        }

        val result = requestUser()

        if (result is Result.Success) {
            setUserModel(result.data)
        }
    }

    /**
     * Refresh access token.
     *
     * If the access token could not be refreshed, a log out is initiated by the [AuthManager].
     */
    suspend fun refreshAccessToken(): Result<AccessToken> {
        val refreshResult = authManager.refreshAccessToken()
        return refreshResult
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

}