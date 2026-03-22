package io.github.drumber.kitsune.data.source.jsonapi.auth.model

import com.fasterxml.jackson.annotation.JsonProperty

data class RefreshAccessToken(
    @JsonProperty("grant_type")
    val grantType: String = "refresh_token",
    @JsonProperty("refresh_token")
    val refreshToken: String
)
