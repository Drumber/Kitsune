package io.github.drumber.kitsune.ui.profile

import io.github.drumber.kitsune.data.mapper.UserMapper.toUser
import io.github.drumber.kitsune.data.presentation.model.user.User
import io.github.drumber.kitsune.data.repository.FollowRepository
import io.github.drumber.kitsune.data.repository.UserRepository
import io.github.drumber.kitsune.domain.user.GetLocalUserIdUseCase
import io.github.drumber.kitsune.testutils.MainDispatcherRule
import io.github.drumber.kitsune.testutils.localUser
import io.github.drumber.kitsune.testutils.onSuspend
import io.github.drumber.kitsune.testutils.useMockedAndroidLogger
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import net.datafaker.Faker
import org.assertj.core.api.Assertions.assertThat
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.KStubbing
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.doThrow
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify

@OptIn(ExperimentalCoroutinesApi::class)
class UserProfileViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val faker = Faker()
    private val userId = "123"

    private fun userRepository(
        user: User? = localUser(faker).toUser(),
        stubbing: KStubbing<UserRepository>.() -> Unit = {}
    ) = mock<UserRepository> {
        onSuspend { fetchUser(any(), any()) } doReturn user
        stubbing()
    }

    private fun followRepository(
        stubbing: KStubbing<FollowRepository>.() -> Unit = {}
    ) = mock<FollowRepository> {
        onSuspend { getFollowId(any(), any()) } doReturn null
        stubbing()
    }

    private fun getLocalUserId(id: String?) = mock<GetLocalUserIdUseCase> {
        on { invoke() } doReturn id
    }

    private fun viewModel(
        userRepository: UserRepository = userRepository(),
        followRepository: FollowRepository = followRepository(),
        getLocalUserId: GetLocalUserIdUseCase = getLocalUserId(null)
    ) = UserProfileViewModel(userId, userRepository, followRepository, getLocalUserId)

    @Test
    fun `loadUser populates the user model on success`() = runTest {
        val user = localUser(faker).toUser()
        val vm = viewModel(userRepository = userRepository(user = user))
        assertThat(vm.getUser()?.id).isEqualTo(user.id)
        assertThat(vm.uiState.value.isInitialLoading).isFalse()
    }

    @Test
    fun `loadUser leaves the user model null when data is null`() = runTest {
        val vm = useMockedAndroidLogger {
            viewModel(userRepository = userRepository(user = null))
        }
        assertThat(vm.getUser()).isNull()
        assertThat(vm.uiState.value.isInitialLoading).isFalse()
    }

    @Test
    fun `loadUser handles fetch errors gracefully`() = runTest {
        val vm = useMockedAndroidLogger {
            viewModel(
                userRepository = userRepository {
                    onSuspend { fetchUser(any(), any()) } doThrow RuntimeException("boom")
                }
            )
        }
        assertThat(vm.getUser()).isNull()
        assertThat(vm.uiState.value.isInitialLoading).isFalse()
    }

    @Test
    fun `loadFollowState disables following when not logged in`() = runTest {
        val followRepository = followRepository()
        viewModel(followRepository = followRepository, getLocalUserId = getLocalUserId(null))
            .also { assertThat(it.uiState.value.canFollow).isFalse() }
        verify(followRepository, never()).getFollowId(any(), any())
    }

    @Test
    fun `loadFollowState disables following on own profile`() = runTest {
        val followRepository = followRepository()
        viewModel(followRepository = followRepository, getLocalUserId = getLocalUserId(userId))
            .also { assertThat(it.uiState.value.canFollow).isFalse() }
        verify(followRepository, never()).getFollowId(any(), any())
    }

    @Test
    fun `loadFollowState resolves following state for another user`() = runTest {
        val vm = viewModel(
            followRepository = followRepository {
                onSuspend { getFollowId(eq("local-1"), eq(userId)) } doReturn "follow-1"
            },
            getLocalUserId = getLocalUserId("local-1")
        )
        val state = vm.uiState.value
        assertThat(state.canFollow).isTrue()
        assertThat(state.isFollowing).isTrue()
        assertThat(state.followId).isEqualTo("follow-1")
    }

    @Test
    fun `loadFollowState marks not following when no follow id exists`() = runTest {
        val vm = viewModel(
            followRepository = followRepository {
                onSuspend { getFollowId(any(), any()) } doReturn null
            },
            getLocalUserId = getLocalUserId("local-1")
        )
        val state = vm.uiState.value
        assertThat(state.canFollow).isTrue()
        assertThat(state.isFollowing).isFalse()
    }

    @Test
    fun `toggleFollow does nothing when not logged in`() = runTest {
        val followRepository = followRepository()
        val vm = viewModel(followRepository = followRepository, getLocalUserId = getLocalUserId(null))
        vm.toggleFollow()
        verify(followRepository, never()).follow(any(), any())
        verify(followRepository, never()).unfollow(any())
    }

    @Test
    fun `toggleFollow does nothing on own profile`() = runTest {
        val followRepository = followRepository()
        val vm = viewModel(followRepository = followRepository, getLocalUserId = getLocalUserId(userId))
        vm.toggleFollow()
        verify(followRepository, never()).follow(any(), any())
        verify(followRepository, never()).unfollow(any())
    }

    @Test
    fun `toggleFollow follows another user`() = runTest {
        val vm = viewModel(
            followRepository = followRepository {
                onSuspend { getFollowId(any(), any()) } doReturn null
                onSuspend { follow(eq("local-1"), eq(userId)) } doReturn "new-follow"
            },
            getLocalUserId = getLocalUserId("local-1")
        )
        vm.toggleFollow()
        val state = vm.uiState.value
        assertThat(state.isFollowing).isTrue()
        assertThat(state.followId).isEqualTo("new-follow")
        assertThat(state.isFollowProcessing).isFalse()
    }

    @Test
    fun `toggleFollow unfollows a followed user`() = runTest {
        val vm = viewModel(
            followRepository = followRepository {
                onSuspend { getFollowId(any(), any()) } doReturn "follow-1"
                onSuspend { unfollow(eq("follow-1")) } doReturn true
            },
            getLocalUserId = getLocalUserId("local-1")
        )
        vm.toggleFollow()
        val state = vm.uiState.value
        assertThat(state.isFollowing).isFalse()
        assertThat(state.followId).isNull()
        assertThat(state.isFollowProcessing).isFalse()
    }
}
