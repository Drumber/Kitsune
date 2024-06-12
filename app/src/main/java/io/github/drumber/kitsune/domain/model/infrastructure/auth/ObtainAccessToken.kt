package io.github.drumber.kitsune.domain.model.infrastructure.auth

import com.fasterxml.jackson.annotation.JsonProperty

data class ObtainAccessToken(
    @JsonProperty("grant_type")
    val grantType: String = "password",
    @JsonProperty("username")
    val username: String,
    @JsonProperty("password")
    val password: String
) {

    companion object {
        fun build(username: String, password: String): ObtainAccessToken {
            return ObtainAccessToken(
                username = username,
                password = password
            )
        }
    }

}
