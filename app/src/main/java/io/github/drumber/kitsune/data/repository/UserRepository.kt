package io.github.drumber.kitsune.data.repository

import io.github.drumber.kitsune.constants.Defaults
import io.github.drumber.kitsune.data.common.exception.NoDataException
import io.github.drumber.kitsune.data.mapper.UserMapper.toLocalUser
import io.github.drumber.kitsune.data.mapper.UserMapper.toUser
import io.github.drumber.kitsune.data.presentation.model.user.User
import io.github.drumber.kitsune.data.source.local.user.UserLocalDataSource
import io.github.drumber.kitsune.data.source.local.user.model.LocalUser
import io.github.drumber.kitsune.data.source.network.user.UserNetworkDataSource
import io.github.drumber.kitsune.domain_old.service.Filter
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow

class UserRepository(
    private val localUserDataSource: UserLocalDataSource,
    private val remoteUserDataSource: UserNetworkDataSource
) {

    private val _localUser = MutableStateFlow(localUserDataSource.loadUser())
    val localUser
        get() = _localUser.asStateFlow()

    private val _userReLogInPrompt = MutableSharedFlow<Unit>()
    val userReLogInPrompt
        get() = _userReLogInPrompt.asSharedFlow()

    fun hasLocalUser() = _localUser.value != null

    fun clearLocalUser() {
        localUserDataSource.clearUser()
        _localUser.value = null
    }

    /**
     * Fetches the app user from the network and updates the local cached user object.
     */
    suspend fun updateLocalUserFromNetwork() {
        val baseFilter = Filter()
            .include("waifu")
            .fields("characters", *Defaults.MINIMUM_CHARACTER_FIELDS)

        val user = remoteUserDataSource.getSelf(baseFilter)?.toLocalUser()
            ?: throw NoDataException()
        storeLocalUser(user)
    }

    fun storeLocalUser(user: LocalUser) {
        localUserDataSource.storeUser(user)
        _localUser.value = user
    }

    fun promptUserReLogIn() {
        _userReLogInPrompt.tryEmit(Unit)
    }

    suspend fun getLocalUserFromNetwork(userId: String, filter: Filter): LocalUser? {
        return remoteUserDataSource.getUser(userId, filter)?.toLocalUser()
    }

    suspend fun getUser(userId: String, filter: Filter): User? {
        return remoteUserDataSource.getUser(userId, filter)?.toUser()
    }
}