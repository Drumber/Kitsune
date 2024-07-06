package io.github.drumber.kitsune.data.source.local.auth.model

import com.fasterxml.jackson.annotation.JsonProperty

data class LocalAccessToken(
    @JsonProperty("access_token")
    val accessToken: String,
    @JsonProperty("created_at")
    val createdAt: Long,
    @JsonProperty("expires_in")
    val expiresIn: Long,
    @JsonProperty("refresh_token")
    val refreshToken: String
) {

    fun getExpirationTimeInSeconds(): Long {
        return createdAt + expiresIn
    }
}
