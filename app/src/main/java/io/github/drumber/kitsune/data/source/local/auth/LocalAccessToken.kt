package io.github.drumber.kitsune.data.source.local.auth

data class LocalAccessToken(
    val accessToken: String,
    val createdAt: Long,
    val expiresIn: Long,
    val refreshToken: String
) {

    fun getExpirationTimeInSeconds(): Long {
        return createdAt + expiresIn
    }

}
