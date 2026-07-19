package io.github.drumber.kitsune.ui.profile.follow

import io.github.drumber.kitsune.data.repository.FollowListType
import io.github.drumber.kitsune.data.repository.FollowRepository
import io.github.drumber.kitsune.domain.user.GetLocalUserIdUseCase
import io.github.drumber.kitsune.testutils.MainDispatcherRule
import io.github.drumber.kitsune.testutils.onSuspend
import io.github.drumber.kitsune.testutils.useMockedAndroidLogger
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.test.runTest
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
class FollowListViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val userId = "profile-user"
    private val localUser = "user-1"
    private val listedUser = "listed-user"

    private fun followRepository(
        stubbing: KStubbing<FollowRepository>.() -> Unit = {}
    ) = mock<FollowRepository> {
        on { followListPager(any(), any(), any()) } doReturn emptyFlow()
        stubbing()
    }

    private fun viewModel(
        listType: FollowListType = FollowListType.FOLLOWING,
        followRepository: FollowRepository = followRepository(),
        getLocalUserId: GetLocalUserIdUseCase = mock { on { invoke() } doReturn localUser }
    ) = FollowListViewModel(userId, listType, followRepository, getLocalUserId)

    @Test
    fun `showButtonFor returns false when there is no local user`() {
        val vm = viewModel(getLocalUserId = mock { on { invoke() } doReturn null })

        assertThat(vm.showButtonFor(listedUser)).isFalse()
    }

    @Test
    fun `showButtonFor returns false for the local user themselves`() {
        val vm = viewModel()

        assertThat(vm.showButtonFor(localUser)).isFalse()
    }

    @Test
    fun `showButtonFor returns true for another user`() {
        val vm = viewModel()

        assertThat(vm.showButtonFor(listedUser)).isTrue()
    }

    @Test
    fun `resolveFollowState does nothing when there is no local user`() = runTest {
        val repository = followRepository()
        val vm = viewModel(
            followRepository = repository,
            getLocalUserId = mock { on { invoke() } doReturn null }
        )

        vm.resolveFollowState(listedUser)

        verify(repository, never()).getFollowId(any(), any())
        assertThat(vm.followStates.value).isEmpty()
    }

    @Test
    fun `resolveFollowState does nothing for the local user themselves`() = runTest {
        val repository = followRepository()
        val vm = viewModel(followRepository = repository)

        vm.resolveFollowState(localUser)

        verify(repository, never()).getFollowId(any(), any())
    }

    @Test
    fun `resolveFollowState marks the user as followed when a follow id exists`() = runTest {
        val repository = followRepository {
            onSuspend { getFollowId(localUser, listedUser) } doReturn "follow-1"
        }
        val vm = viewModel(followRepository = repository)

        vm.resolveFollowState(listedUser)

        val state = vm.followStates.value[listedUser]
        assertThat(state?.isResolved).isTrue()
        assertThat(state?.isFollowing).isTrue()
        assertThat(state?.followId).isEqualTo("follow-1")
    }

    @Test
    fun `resolveFollowState marks the user as not followed when no follow id exists`() = runTest {
        val repository = followRepository {
            onSuspend { getFollowId(localUser, listedUser) } doReturn null
        }
        val vm = viewModel(followRepository = repository)

        vm.resolveFollowState(listedUser)

        val state = vm.followStates.value[listedUser]
        assertThat(state?.isResolved).isTrue()
        assertThat(state?.isFollowing).isFalse()
        assertThat(state?.followId).isNull()
    }

    @Test
    fun `resolveFollowState does not query again once resolved`() = runTest {
        val repository = followRepository {
            onSuspend { getFollowId(localUser, listedUser) } doReturn "follow-1"
        }
        val vm = viewModel(followRepository = repository)

        vm.resolveFollowState(listedUser)
        vm.resolveFollowState(listedUser)

        verify(repository).getFollowId(eq(localUser), eq(listedUser))
    }

    @Test
    fun `resolveFollowState leaves state unresolved when the repository throws`() = runTest {
        useMockedAndroidLogger {
            val repository = followRepository {
                onSuspend { getFollowId(localUser, listedUser) } doThrow RuntimeException("boom")
            }
            val vm = viewModel(followRepository = repository)

            vm.resolveFollowState(listedUser)

            assertThat(vm.followStates.value[listedUser]?.isResolved).isNotEqualTo(true)
        }
    }

    @Test
    fun `toggleFollow does nothing when there is no local user`() = runTest {
        val repository = followRepository()
        val vm = viewModel(
            followRepository = repository,
            getLocalUserId = mock { on { invoke() } doReturn null }
        )

        vm.toggleFollow(listedUser)

        verify(repository, never()).follow(any(), any())
        verify(repository, never()).unfollow(any())
    }

    @Test
    fun `toggleFollow does nothing for the local user themselves`() = runTest {
        val repository = followRepository()
        val vm = viewModel(followRepository = repository)

        vm.toggleFollow(localUser)

        verify(repository, never()).follow(any(), any())
        verify(repository, never()).unfollow(any())
    }

    @Test
    fun `toggleFollow follows a user that is not yet followed`() = runTest {
        val repository = followRepository {
            onSuspend { follow(localUser, listedUser) } doReturn "follow-1"
        }
        val vm = viewModel(followRepository = repository)

        vm.toggleFollow(listedUser)

        val state = vm.followStates.value[listedUser]
        assertThat(state?.isFollowing).isTrue()
        assertThat(state?.followId).isEqualTo("follow-1")
        assertThat(state?.isProcessing).isFalse()
    }

    @Test
    fun `toggleFollow keeps state unfollowed when follow fails`() = runTest {
        val repository = followRepository {
            onSuspend { follow(localUser, listedUser) } doReturn null
        }
        val vm = viewModel(followRepository = repository)

        vm.toggleFollow(listedUser)

        val state = vm.followStates.value[listedUser]
        assertThat(state?.isFollowing).isFalse()
        assertThat(state?.isProcessing).isFalse()
    }

    @Test
    fun `toggleFollow unfollows a user that is currently followed`() = runTest {
        val repository = followRepository {
            onSuspend { getFollowId(localUser, listedUser) } doReturn "follow-1"
            onSuspend { unfollow("follow-1") } doReturn true
        }
        val vm = viewModel(followRepository = repository)

        vm.resolveFollowState(listedUser)
        vm.toggleFollow(listedUser)

        val state = vm.followStates.value[listedUser]
        assertThat(state?.isFollowing).isFalse()
        assertThat(state?.followId).isNull()
        assertThat(state?.isProcessing).isFalse()
    }
}
