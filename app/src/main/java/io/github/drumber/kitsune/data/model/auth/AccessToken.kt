package io.github.drumber.kitsune.data.model.auth

import com.fasterxml.jackson.annotation.JsonProperty

/**
 * @param createdAt time in seconds the access token was created
 * @param expiresIn seconds until the [accessToken] expires (30 days default)
 */
data class AccessToken(
    @JsonProperty("access_token") val accessToken: String?,
    @JsonProperty("created_at") val createdAt: Long?,
    @JsonProperty("expires_in") val expiresIn: Long?,
    @JsonProperty("refresh_token") val refreshToken: String?,
    @JsonProperty("scope") val scope: String?,
    @JsonProperty("token_type") val tokenType: String?,
)
