package io.github.drumber.kitsune.data.source.network.auth

import io.github.drumber.kitsune.data.source.network.auth.api.AuthenticationApi
import io.github.drumber.kitsune.data.source.network.auth.model.NetworkAccessToken
import io.github.drumber.kitsune.data.source.network.auth.model.ObtainAccessToken
import io.github.drumber.kitsune.data.source.network.auth.model.RefreshAccessToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AccessTokenNetworkDataSource(
    private val authenticationApi: AuthenticationApi
) {

    suspend fun obtainAccessToken(obtainAccessToken: ObtainAccessToken): NetworkAccessToken {
        return withContext(Dispatchers.IO) {
            authenticationApi.obtainAccessToken(obtainAccessToken)
        }
    }

    suspend fun refreshToken(refreshAccessToken: RefreshAccessToken): NetworkAccessToken {
        return withContext(Dispatchers.IO) {
            authenticationApi.refreshToken(refreshAccessToken)
        }
    }

}