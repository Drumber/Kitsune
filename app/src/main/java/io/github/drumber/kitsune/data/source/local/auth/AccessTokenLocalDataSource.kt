package io.github.drumber.kitsune.data.source.local.auth

interface AccessTokenLocalDataSource {

    fun loadAccessToken(): LocalAccessToken?

    fun storeAccessToken(accessToken: LocalAccessToken)

    fun clearAccessToken()

}