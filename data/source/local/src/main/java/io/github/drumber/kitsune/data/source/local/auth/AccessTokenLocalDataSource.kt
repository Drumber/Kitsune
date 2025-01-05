package io.github.drumber.kitsune.data.source.local.auth

import io.github.drumber.kitsune.data.source.local.auth.model.LocalAccessToken

interface AccessTokenLocalDataSource {

    fun loadAccessToken(): LocalAccessToken?

    fun storeAccessToken(accessToken: LocalAccessToken)

    fun clearAccessToken()

}