package io.github.drumber.kitsune.ui.reactiondetail

import app.cash.turbine.test
import io.github.drumber.kitsune.data.presentation.model.reaction.MediaReaction
import io.github.drumber.kitsune.data.repository.MediaReactionRepository
import io.github.drumber.kitsune.domain.user.GetLocalUserIdUseCase
import io.github.drumber.kitsune.testutils.MainDispatcherRule
import io.github.drumber.kitsune.testutils.onSuspend
import io.github.drumber.kitsune.testutils.useMockedAndroidLogger
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import net.datafaker.Faker
import org.assertj.core.api.Assertions.assertThat
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.doThrow
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify

@OptIn(ExperimentalCoroutinesApi::class)
class ReactionDetailViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val faker = Faker()

    private val reactionId = "reaction-1"

    private fun reaction(upVotesCount: Int = 5) = MediaReaction(
        id = reactionId,
        createdAt = faker.date().birthday().toString(),
        reaction = faker.text().text(5),
        content = faker.text().text(),
        contentFormatted = faker.text().text(),
        upVotesCount = upVotesCount,
        authorId = faker.number().positive().toString(),
        authorName = faker.name().username(),
        authorAvatarUrl = faker.internet().url(),
        mediaId = faker.number().positive().toString(),
        mediaTitle = faker.book().title(),
        mediaPosterUrl = faker.internet().url(),
        mediaSlug = faker.internet().slug(),
        mediaIsAnime = true
    )

    private fun viewModel(
        mediaReactionRepository: MediaReactionRepository = mock(),
        getLocalUserId: GetLocalUserIdUseCase = mock()
    ) = ReactionDetailViewModel(reactionId, mediaReactionRepository, getLocalUserId)

    @Test
    fun `loadReaction populates reaction and stops loading on success`() = runTest {
        val expected = reaction()
        val vm = viewModel(
            mediaReactionRepository = mock {
                onSuspend { getReaction(reactionId) } doReturn expected
            }
        )

        assertThat(vm.reaction.value).isEqualTo(expected)
        assertThat(vm.isLoading.value).isFalse()
    }

    @Test
    fun `loadReaction leaves reaction null and stops loading when repository returns null`() = runTest {
        val vm = viewModel(
            mediaReactionRepository = mock {
                onSuspend { getReaction(reactionId) } doReturn null
            }
        )

        assertThat(vm.reaction.value).isNull()
        assertThat(vm.isLoading.value).isFalse()
    }

    @Test
    fun `loadReaction stops loading when repository throws`() = runTest {
        useMockedAndroidLogger {
            val vm = viewModel(
                mediaReactionRepository = mock {
                    onSuspend { getReaction(reactionId) } doThrow RuntimeException("boom")
                }
            )

            assertThat(vm.reaction.value).isNull()
            assertThat(vm.isLoading.value).isFalse()
        }
    }

    @Test
    fun `upvote emits LoginRequired when there is no local user`() = runTest {
        val vm = viewModel(getLocalUserId = mock { on { invoke() } doReturn null })

        vm.upvote()

        assertThat(vm.events.first()).isEqualTo(ReactionDetailViewModel.Event.LoginRequired)
    }

    @Test
    fun `upvote does nothing when the reaction has not been loaded`() = runTest {
        val repository = mock<MediaReactionRepository> {
            onSuspend { getReaction(reactionId) } doReturn null
        }
        val vm = viewModel(
            mediaReactionRepository = repository,
            getLocalUserId = mock { on { invoke() } doReturn "user-1" }
        )

        vm.upvote()

        verify(repository, never()).upvoteReaction(eq("user-1"), eq(reactionId))
    }

    @Test
    fun `upvote increments the count and marks upvoted on success`() = runTest {
        val repository = mock<MediaReactionRepository> {
            onSuspend { getReaction(reactionId) } doReturn reaction(upVotesCount = 5)
            onSuspend { upvoteReaction("user-1", reactionId) } doReturn true
        }
        val vm = viewModel(
            mediaReactionRepository = repository,
            getLocalUserId = mock { on { invoke() } doReturn "user-1" }
        )

        vm.events.test {
            vm.upvote()
            assertThat(awaitItem()).isEqualTo(ReactionDetailViewModel.Event.UpvoteSuccess(6))
        }
        assertThat(vm.isUpvoted.value).isTrue()
        assertThat(vm.reaction.value?.upVotesCount).isEqualTo(6)
    }

    @Test
    fun `upvote is ignored when already upvoted`() = runTest {
        val repository = mock<MediaReactionRepository> {
            onSuspend { getReaction(reactionId) } doReturn reaction(upVotesCount = 5)
            onSuspend { upvoteReaction("user-1", reactionId) } doReturn true
        }
        val vm = viewModel(
            mediaReactionRepository = repository,
            getLocalUserId = mock { on { invoke() } doReturn "user-1" }
        )

        vm.upvote()
        vm.upvote()

        verify(repository).upvoteReaction(eq("user-1"), eq(reactionId))
    }

    @Test
    fun `upvote emits Failed when the repository returns false`() = runTest {
        val repository = mock<MediaReactionRepository> {
            onSuspend { getReaction(reactionId) } doReturn reaction()
            onSuspend { upvoteReaction("user-1", reactionId) } doReturn false
        }
        val vm = viewModel(
            mediaReactionRepository = repository,
            getLocalUserId = mock { on { invoke() } doReturn "user-1" }
        )

        vm.events.test {
            vm.upvote()
            assertThat(awaitItem()).isEqualTo(ReactionDetailViewModel.Event.UpvoteFailed)
        }
        assertThat(vm.isUpvoted.value).isFalse()
    }

    @Test
    fun `upvote emits Failed when the repository throws`() = runTest {
        useMockedAndroidLogger {
            val repository = mock<MediaReactionRepository> {
                onSuspend { getReaction(reactionId) } doReturn reaction()
                onSuspend { upvoteReaction("user-1", reactionId) } doThrow RuntimeException("boom")
            }
            val vm = viewModel(
                mediaReactionRepository = repository,
                getLocalUserId = mock { on { invoke() } doReturn "user-1" }
            )

            vm.events.test {
                vm.upvote()
                assertThat(awaitItem()).isEqualTo(ReactionDetailViewModel.Event.UpvoteFailed)
            }
            assertThat(vm.isUpvoted.value).isFalse()
        }
    }
}
