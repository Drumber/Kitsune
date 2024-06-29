package io.github.drumber.kitsune.data.mapper

import io.github.drumber.kitsune.data.source.local.auth.LocalAccessToken
import io.github.drumber.kitsune.data.source.network.auth.model.NetworkAccessToken

object AuthMapper {
    fun NetworkAccessToken.toLocalAccessToken() = LocalAccessToken(
        accessToken = accessToken.require(),
        createdAt = createdAt.require(),
        expiresIn = expiresIn.require(),
        refreshToken = refreshToken.require()
    )
}
