package io.github.drumber.kitsune.data.source.local.auth

import io.github.drumber.kitsune.data.source.local.auth.model.LocalAccessToken
import net.datafaker.Faker
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class LocalAccessTokenTest {

    private val faker = Faker()

    @Test
    fun shouldGetExpirationTimeInSeconds() {
        // given
        val localAccessToken = LocalAccessToken(
            accessToken = faker.lorem().word(),
            refreshToken = faker.lorem().word(),
            createdAt = 1000,
            expiresIn = 5000
        )

        // when
        val expirationTimeInSeconds = localAccessToken.getExpirationTimeInSeconds()

        // then
        assertThat(expirationTimeInSeconds).isEqualTo(6000)
    }
}