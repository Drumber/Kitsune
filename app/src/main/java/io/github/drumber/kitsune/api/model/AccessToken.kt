package io.github.drumber.kitsune.api.model

import com.fasterxml.jackson.annotation.JsonProperty

/**
 * @param createdAt time in seconds the access token was created
 * @param expiresIn seconds until the [accessToken] expires (30 days default)
 */
data class AccessToken(
    @JsonProperty("access_token") var accessToken: String? = null,
    @JsonProperty("created_at") var createdAt: Long? = null,
    @JsonProperty("expires_in") var expiresIn: Long? = null,
    @JsonProperty("refresh_token") var refreshToken: String? = null,
    @JsonProperty("scope") var scope: String? = null,
    @JsonProperty("token_type") var tokenType: String? = null,
)
