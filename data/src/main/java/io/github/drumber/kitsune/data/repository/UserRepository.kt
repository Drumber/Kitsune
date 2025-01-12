package io.github.drumber.kitsune.data.repository

import io.github.drumber.kitsune.data.exception.NoDataException
import io.github.drumber.kitsune.data.mapper.ProfileLinksMapper.toProfileLink
import io.github.drumber.kitsune.data.mapper.UserMapper.toLocalUser
import io.github.drumber.kitsune.data.mapper.UserMapper.toNetworkUser
import io.github.drumber.kitsune.data.mapper.UserMapper.toUser
import io.github.drumber.kitsune.data.model.Filter
import io.github.drumber.kitsune.data.model.user.User
import io.github.drumber.kitsune.data.model.user.profilelinks.ProfileLink
import io.github.drumber.kitsune.data.source.jsonapi.user.UserNetworkDataSource
import io.github.drumber.kitsune.data.source.jsonapi.user.model.NetworkUserImageUpload
import io.github.drumber.kitsune.data.source.local.user.UserLocalDataSource
import io.github.drumber.kitsune.data.source.local.user.model.LocalUser
import io.github.drumber.kitsune.shared.constants.Defaults
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class UserRepository(
    private val localUserDataSource: UserLocalDataSource,
    private val remoteUserDataSource: UserNetworkDataSource,
    private val coroutineScope: CoroutineScope
) {

    private val storeLocalUserMutex = Mutex()

    private val _localUser = MutableStateFlow(localUserDataSource.loadUser())
    val localUser = _localUser.asStateFlow()

    private val _userReLogInPrompt = MutableSharedFlow<Unit>()
    val userReLogInPrompt = _userReLogInPrompt.asSharedFlow()

    fun hasLocalUser() = _localUser.value != null

    fun clearLocalUser() {
        localUserDataSource.clearUser()
        _localUser.value = null
    }

    /**
     * Fetches the app user from the network and updates the local cached user object.
     */
    suspend fun fetchAndStoreLocalUserFromNetwork() {
        val baseFilter = Filter()
            .include("waifu")
            .fields("characters", *Defaults.MINIMUM_CHARACTER_FIELDS)

        // fetch user model in the repository scope
        coroutineScope.async {
            val user = remoteUserDataSource.getSelf(baseFilter)?.toLocalUser()
                ?: throw NoDataException()
            storeLocalUser(user)
        }.await()
    }

    suspend fun storeLocalUser(user: LocalUser) {
        storeLocalUserMutex.withLock {
            localUserDataSource.storeUser(user)
            _localUser.value = user
        }
    }

    suspend fun promptUserReLogIn() {
        _userReLogInPrompt.emit(Unit)
    }

    suspend fun fetchUser(userId: String, filter: Filter): User? {
        return remoteUserDataSource.getUser(userId, filter)?.toUser()
    }

    suspend fun updateUser(userId: String, user: LocalUser): LocalUser? {
        return remoteUserDataSource.updateUser(userId, user.toNetworkUser())?.toLocalUser()
    }

    suspend fun updateUserImage(userId: String, avatar: String?, coverImage: String?): Boolean {
        val userImageUpload = NetworkUserImageUpload(
            id = userId,
            avatar = avatar,
            coverImage = coverImage
        )
        return remoteUserDataSource.updateUserImage(userId, userImageUpload)
    }

    suspend fun deleteWaifuRelationship(userId: String): Boolean {
        return remoteUserDataSource.deleteWaifuRelationship(userId)
    }

    suspend fun getProfileLinksForUser(userId: String, filter: Filter): List<ProfileLink>? {
        return remoteUserDataSource.getProfileLinksForUser(userId, filter)
            ?.map { it.toProfileLink() }
    }
}