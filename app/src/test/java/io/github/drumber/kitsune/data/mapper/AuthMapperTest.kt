package io.github.drumber.kitsune.data.mapper

import io.github.drumber.kitsune.data.mapper.AuthMapper.toLocalAccessToken
import io.github.drumber.kitsune.data.source.network.auth.model.NetworkAccessToken
import net.datafaker.Faker
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class AuthMapperTest {

    private val faker = Faker()

    @Test
    fun shouldMap_NetworkAccessToken_to_LocalAccessToken() {
        // given
        val networkAccessToken = NetworkAccessToken(
            accessToken = faker.lorem().word(),
            createdAt = faker.number().randomNumber(),
            expiresIn = faker.number().randomNumber(),
            refreshToken = faker.lorem().word(),
            tokenType = "bearer",
            scope = "public"
        )

        // when
        val localAccessToken = networkAccessToken.toLocalAccessToken()

        // then
        assertThat(localAccessToken)
            .usingRecursiveComparison()
            .isEqualTo(networkAccessToken)
    }
}