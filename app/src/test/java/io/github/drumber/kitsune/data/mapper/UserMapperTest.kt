package io.github.drumber.kitsune.data.mapper

import io.github.drumber.kitsune.data.mapper.UserMapper.toLocalUser
import io.github.drumber.kitsune.data.mapper.UserMapper.toUser
import io.github.drumber.kitsune.testutils.localUser
import io.github.drumber.kitsune.testutils.networkUser
import net.datafaker.Faker
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class UserMapperTest {

    private val faker = Faker()

    @Test
    fun shouldMap_NetworkUser_to_LocalUser() {
        // given
        val networkUser = networkUser(faker)

        // when
        val localUser = networkUser.toLocalUser()

        // then
        assertThat(localUser)
            .usingRecursiveComparison()
            .isEqualTo(networkUser)
    }

    @Test
    fun shouldMap_NetworkUser_to_User() {
        // given
        val networkUser = networkUser(faker)

        // when
        val user = networkUser.toUser()

        // then
        assertThat(user)
            .usingRecursiveComparison()
            .isEqualTo(networkUser)
    }

    @Test
    fun shouldMap_LocalUser_to_NetworkUser() {
        // given
        val localUser = localUser(faker)

        // when
        val user = localUser.toUser()

        // then
        assertThat(user)
            .usingRecursiveComparison()
            .ignoringFields("favorites", "profileLinks", "stats", "waifu.mediaCharacters")
            .isEqualTo(localUser)
    }
}