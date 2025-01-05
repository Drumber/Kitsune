package io.github.drumber.kitsune.data.repository

import io.github.drumber.kitsune.data.source.jsonapi.user.UserNetworkDataSource
import io.github.drumber.kitsune.data.source.local.user.UserLocalDataSource
import io.github.drumber.kitsune.testutils.localUser
import io.github.drumber.kitsune.testutils.networkUser
import io.github.drumber.kitsune.testutils.onSuspend
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
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
    fun shouldFetchAndStoreLocalUserFromNetwork() = runTest {
        // given
        val user = networkUser(faker)

        val localUserDataSource = mock<UserLocalDataSource> {
            doNothing().on { storeUser(any()) }
        }

        val remoteUserDataSource = mock<UserNetworkDataSource> {
            onSuspend { getSelf(any()) } doReturn user
        }

        val userRepository = UserRepository(localUserDataSource, remoteUserDataSource, backgroundScope)

        // when
        userRepository.fetchAndStoreLocalUserFromNetwork()

        // then
        verify(remoteUserDataSource).getSelf(any())
        verify(localUserDataSource).storeUser(any())
        assertThat(userRepository.localUser.value).isNotNull
    }

    @Test
    fun shouldStoreLocalUser() = runTest {
        // given
        val user = localUser(faker)

        val localUserDataSource = mock<UserLocalDataSource> {
            doNothing().on { storeUser(any()) }
        }

        val remoteUserDataSource = mock<UserNetworkDataSource>()

        val userRepository = UserRepository(localUserDataSource, remoteUserDataSource, backgroundScope)

        // when
        userRepository.storeLocalUser(user)

        // then
        verify(localUserDataSource).storeUser(any())
        assertThat(userRepository.localUser.value).isEqualTo(user)
    }

    @Test
    fun shouldExposeLocalUserAsStateFlow() = runTest {
        // given
        val networkUser = networkUser(faker)
        var user = localUser(faker)

        val localUserDataSource = mock<UserLocalDataSource> {
            doNothing().on { storeUser(any()) }
            on { loadUser() } doReturn user
        }

        val remoteUserDataSource = mock<UserNetworkDataSource> {
            onSuspend { getSelf(any()) } doReturn networkUser
        }

        val userRepository = UserRepository(localUserDataSource, remoteUserDataSource, backgroundScope)

        // when & then
        verify(localUserDataSource).loadUser()
        assertThat(userRepository.localUser.value).isEqualTo(user)
        assertThat(userRepository.hasLocalUser()).isTrue()

        user = user.copy(id = "2")
        userRepository.storeLocalUser(user)
        assertThat(userRepository.localUser.value).isEqualTo(user)

        userRepository.clearLocalUser()
        assertThat(userRepository.localUser.value).isNull()
        assertThat(userRepository.hasLocalUser()).isFalse()

        userRepository.fetchAndStoreLocalUserFromNetwork()
        assertThat(userRepository.localUser.value?.id).isEqualTo(networkUser.id)
        assertThat(userRepository.hasLocalUser()).isTrue()
    }

    @Test
    fun shouldTriggerReLogInPrompt() = runTest {
        // given
        val localUserDataSource = mock<UserLocalDataSource>()
        val remoteUserDataSource = mock<UserNetworkDataSource>()

        val userRepository = UserRepository(localUserDataSource, remoteUserDataSource, backgroundScope)

        // when
        val results = mutableListOf<Unit>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            userRepository.userReLogInPrompt.toList(results)
        }

        userRepository.promptUserReLogIn()
        userRepository.promptUserReLogIn()

        // then
        assertThat(results).hasSize(2)
    }
}