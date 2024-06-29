package io.github.drumber.kitsune.data.repository

import io.github.drumber.kitsune.data.mapper.UserMapper.toLocalUser
import io.github.drumber.kitsune.data.source.local.user.UserLocalDataSource
import io.github.drumber.kitsune.data.source.network.user.UserNetworkDataSource
import io.github.drumber.kitsune.testutils.networkUser
import io.github.drumber.kitsune.testutils.onSuspend
import kotlinx.coroutines.runBlocking
import net.datafaker.Faker
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.doNothing
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify

class UserRepositoryTest {

    private val faker = Faker()

    @Test
    fun shouldUpdateLocalUserFromNetwork(): Unit = runBlocking {
        // given
        val user = networkUser(faker)

        val localUserDataSource = mock<UserLocalDataSource> {
            doNothing().on { storeUser(any()) }
        }

        val remoteUserDataSource = mock<UserNetworkDataSource> {
            onSuspend { getSelf(any()) } doReturn user
        }

        val userRepository = UserRepository(localUserDataSource, remoteUserDataSource)

        // when
        userRepository.updateLocalUserFromNetwork()

        // then
        verify(remoteUserDataSource).getSelf(any())
        verify(localUserDataSource).storeUser(any())
        assertThat(userRepository.localUser.value).isNotNull
    }

    @Test
    fun shouldStoreLocalUser() {
        // given
        val user = networkUser(faker).toLocalUser()

        val localUserDataSource = mock<UserLocalDataSource> {
            doNothing().on { storeUser(any()) }
        }

        val remoteUserDataSource = mock<UserNetworkDataSource>()

        val userRepository = UserRepository(localUserDataSource, remoteUserDataSource)

        // when
        userRepository.storeLocalUser(user)

        // then
        verify(localUserDataSource).storeUser(any())
        assertThat(userRepository.localUser.value).isEqualTo(user)
    }
}