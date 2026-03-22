package io.github.drumber.kitsune.data.source.jsonapi.auth.model

import com.fasterxml.jackson.annotation.JsonProperty

data class ObtainAccessToken(
    @JsonProperty("grant_type")
    val grantType: String = "password",
    @JsonProperty("username")
    val username: String,
    @JsonProperty("password")
    val password: String
)
