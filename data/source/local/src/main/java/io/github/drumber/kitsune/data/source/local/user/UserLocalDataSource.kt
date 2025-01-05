package io.github.drumber.kitsune.data.source.local.user

import io.github.drumber.kitsune.data.source.local.user.model.LocalUser

interface UserLocalDataSource {

    fun loadUser(): LocalUser?

    fun storeUser(user: LocalUser)

    fun clearUser()

}