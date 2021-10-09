package io.github.drumber.kitsune.data.repository

import io.github.drumber.kitsune.data.Result
import io.github.drumber.kitsune.data.model.auth.User
import io.github.drumber.kitsune.data.service.Filter
import io.github.drumber.kitsune.data.service.user.UserService
import io.github.drumber.kitsune.exception.ReceivedDataException
import io.github.drumber.kitsune.util.logI

class UserRepository(val service: UserService, private val authRepository: AuthRepository) {

    var user: User? = null
        private set

    val hasUser: Boolean
        get() = user != null

    init {
        // TODO: load stored user cache
        user = null
    }

    fun logOut() {
        authRepository.logout()
        user = null
    }

    private suspend fun requestUser(): Result<User> {
        val filter = Filter().filter("self", "true")
        return try {
            val userModel = service.allUsers(filter.options).get()?.first()
                ?: throw ReceivedDataException("Received invalid user data.")
            Result.Success(userModel)
        } catch (e: Exception) {
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
        authRepository.refreshAccessTokenIfExpired()

        if (!authRepository.isLoggedIn) {
            logI("Cannot update user cache: No access token available.")
            return
        }

        val result = requestUser()

        if (result is Result.Success) {
            setUserModel(result.data)
        }
    }

    private fun setUserModel(user: User) {
        this.user = user
    }

}