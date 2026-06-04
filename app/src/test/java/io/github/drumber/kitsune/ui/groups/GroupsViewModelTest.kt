package io.github.drumber.kitsune.ui.groups

import androidx.paging.PagingData
import app.cash.turbine.test
import io.github.drumber.kitsune.data.presentation.model.group.Group
import io.github.drumber.kitsune.data.presentation.model.group.GroupCategory
import io.github.drumber.kitsune.data.repository.GroupsRepository
import io.github.drumber.kitsune.domain.user.GetLocalUserIdUseCase
import io.github.drumber.kitsune.testutils.MainDispatcherRule
import io.github.drumber.kitsune.testutils.onSuspend
import io.github.drumber.kitsune.testutils.useMockedAndroidLogger
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import net.datafaker.Faker
import org.assertj.core.api.Assertions.assertThat
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.KStubbing
import org.mockito.kotlin.any
import org.mockito.kotlin.anyOrNull
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.doThrow
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify

@OptIn(ExperimentalCoroutinesApi::class)
class GroupsViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val faker = Faker()

    private fun category() = GroupCategory(
        id = faker.number().positive().toString(),
        name = faker.book().genre(),
        slug = faker.internet().slug()
    )

    private fun groupsRepository(
        stubbing: KStubbing<GroupsRepository>.() -> Unit = {}
    ): GroupsRepository = mock {
        on { groupsPager(anyOrNull(), anyOrNull(), any(), any()) } doReturn
            flowOf(PagingData.empty<Group>())
        on { followedGroupsPager(any(), anyOrNull(), any()) } doReturn
            flowOf(PagingData.empty<Group>())
        stubbing()
    }

    private fun getLocalUserId(id: String?) = mock<GetLocalUserIdUseCase> {
        on { invoke() } doReturn id
    }

    @Test
    fun `isLoggedIn reflects the local user id`() = runTest {
        val loggedIn = GroupsViewModel(groupsRepository(), getLocalUserId("user-1"))
        val loggedOut = GroupsViewModel(groupsRepository(), getLocalUserId(null))

        assertThat(loggedIn.isLoggedIn).isTrue()
        assertThat(loggedOut.isLoggedIn).isFalse()
    }

    @Test
    fun `following is enabled by default when logged in`() = runTest {
        val vm = GroupsViewModel(groupsRepository(), getLocalUserId("user-1"))

        assertThat(vm.isFollowingEnabled.value).isTrue()
    }

    @Test
    fun `following is disabled by default when logged out`() = runTest {
        val vm = GroupsViewModel(groupsRepository(), getLocalUserId(null))

        assertThat(vm.isFollowingEnabled.value).isFalse()
    }

    @Test
    fun `dataSource uses the followed pager when logged in and following enabled`() = runTest {
        val repository = groupsRepository()
        val vm = GroupsViewModel(repository, getLocalUserId("user-1"))

        vm.dataSource.test {
            awaitItem()
            cancelAndIgnoreRemainingEvents()
        }

        verify(repository).followedGroupsPager(eq("user-1"), anyOrNull(), any())
        verify(repository, never()).groupsPager(anyOrNull(), anyOrNull(), any(), any())
    }

    @Test
    fun `dataSource uses the public pager when logged out`() = runTest {
        val repository = groupsRepository()
        val vm = GroupsViewModel(repository, getLocalUserId(null))

        vm.dataSource.test {
            awaitItem()
            cancelAndIgnoreRemainingEvents()
        }

        verify(repository).groupsPager(anyOrNull(), anyOrNull(), any(), any())
        verify(repository, never()).followedGroupsPager(any(), anyOrNull(), any())
    }

    @Test
    fun `disabling following switches to the public pager`() = runTest {
        val repository = groupsRepository()
        val vm = GroupsViewModel(repository, getLocalUserId("user-1"))

        vm.dataSource.test {
            awaitItem()
            vm.setFollowingEnabled(false)
            awaitItem()
            cancelAndIgnoreRemainingEvents()
        }

        assertThat(vm.isFollowingEnabled.value).isFalse()
        verify(repository).groupsPager(anyOrNull(), anyOrNull(), any(), any())
    }

    @Test
    fun `setCategory updates the selected category`() = runTest {
        val vm = GroupsViewModel(groupsRepository(), getLocalUserId(null))

        vm.setCategory("category-7")

        assertThat(vm.selectedCategoryId.value).isEqualTo("category-7")
    }

    @Test
    fun `init loads categories from the repository`() = runTest {
        val categories = listOf(category(), category())
        val vm = GroupsViewModel(
            groupsRepository { onSuspend { getCategories() } doReturn categories },
            getLocalUserId(null)
        )

        assertThat(vm.categories.value).isEqualTo(categories)
    }

    @Test
    fun `init leaves categories empty when loading throws`() = runTest {
        useMockedAndroidLogger {
            val vm = GroupsViewModel(
                groupsRepository { onSuspend { getCategories() } doThrow RuntimeException("boom") },
                getLocalUserId(null)
            )

            assertThat(vm.categories.value).isEmpty()
        }
    }
}
