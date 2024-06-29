package io.github.drumber.kitsune.data.mapper

import io.github.drumber.kitsune.data.mapper.UserMapper.toLocalUser
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
}