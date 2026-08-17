package io.github.drumber.kitsune.ui.groupdetail

import io.github.drumber.kitsune.data.presentation.model.group.Group
import io.github.drumber.kitsune.data.repository.GroupsRepository
import io.github.drumber.kitsune.domain.user.GetLocalUserIdUseCase
import io.github.drumber.kitsune.testutils.MainDispatcherRule
import io.github.drumber.kitsune.testutils.onSuspend
import io.github.drumber.kitsune.testutils.useMockedAndroidLogger
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import net.datafaker.Faker
import org.assertj.core.api.Assertions.assertThat
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.doThrow
import org.mockito.kotlin.mock

@OptIn(ExperimentalCoroutinesApi::class)
class GroupDetailViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val faker = Faker()
    private val groupId = "group-1"

    private fun group() = Group(
        id = groupId,
        createdAt = faker.date().birthday().toString(),
        lastActivityAt = faker.date().birthday().toString(),
        name = faker.book().title(),
        slug = faker.internet().slug(),
        tagline = faker.text().text(),
        about = faker.text().text(),
        rules = faker.text().text(),
        rulesFormatted = faker.text().text(),
        privacy = "open",
        nsfw = false,
        featured = true,
        membersCount = faker.number().numberBetween(1, 1000),
        leadersCount = faker.number().numberBetween(1, 10),
        avatar = faker.internet().url(),
        coverImageUrl = faker.internet().url(),
        categoryId = faker.number().positive().toString(),
        categoryName = faker.book().genre()
    )

    private fun viewModel(
        groupsRepository: GroupsRepository = mock(),
        getLocalUserId: GetLocalUserIdUseCase = mock()
    ) = GroupDetailViewModel(groupId, groupsRepository, getLocalUserId)

    @Test
    fun `loadGroup populates group and stops loading on success`() = runTest {
        val expected = group()
        val vm = viewModel(
            groupsRepository = mock {
                onSuspend { getGroup(groupId) } doReturn expected
            }
        )

        assertThat(vm.group.value).isEqualTo(expected)
        assertThat(vm.isLoading.value).isFalse()
    }

    @Test
    fun `loadGroup leaves group null and stops loading when repository returns null`() = runTest {
        val vm = viewModel(
            groupsRepository = mock {
                onSuspend { getGroup(groupId) } doReturn null
            }
        )

        assertThat(vm.group.value).isNull()
        assertThat(vm.isLoading.value).isFalse()
    }

    @Test
    fun `loadGroup stops loading when repository throws`() = runTest {
        useMockedAndroidLogger {
            val vm = viewModel(
                groupsRepository = mock {
                    onSuspend { getGroup(groupId) } doThrow RuntimeException("boom")
                }
            )

            assertThat(vm.group.value).isNull()
            assertThat(vm.isLoading.value).isFalse()
        }
    }
}
