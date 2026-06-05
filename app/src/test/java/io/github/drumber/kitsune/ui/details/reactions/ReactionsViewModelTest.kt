package io.github.drumber.kitsune.ui.details.reactions

import app.cash.turbine.test
import androidx.paging.PagingData
import io.github.drumber.kitsune.data.presentation.model.reaction.MediaReaction
import io.github.drumber.kitsune.data.repository.LibraryRepository
import io.github.drumber.kitsune.data.repository.MediaReactionRepository
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
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.doThrow
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify

@OptIn(ExperimentalCoroutinesApi::class)
class ReactionsViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val faker = Faker()

    private fun reaction(upVotesCount: Int = 5) = MediaReaction(
        id = "reaction-1",
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

    private fun reactionRepository(
        stubbing: KStubbing<MediaReactionRepository>.() -> Unit = {}
    ): MediaReactionRepository = mock {
        on { reactionsPager(any(), any(), any()) } doReturn flowOf(PagingData.empty<MediaReaction>())
        stubbing()
    }

    private fun getLocalUserId(id: String?) = mock<GetLocalUserIdUseCase> {
        on { invoke() } doReturn id
    }

    private fun libraryRepository(): LibraryRepository = mock()

    @Test
    fun `dataSource requests a pager for the selected media`() = runTest {
        val repository = reactionRepository()
        val vm = ReactionsViewModel(repository, getLocalUserId(null), libraryRepository())

        vm.dataSource.test {
            vm.setMedia("media-1", isAnime = true)
            awaitItem()
            cancelAndIgnoreRemainingEvents()
        }

        verify(repository).reactionsPager(eq(true), eq("media-1"), any())
    }

    @Test
    fun `upvote emits LoginRequired when not logged in`() = runTest {
        val vm = ReactionsViewModel(reactionRepository(), getLocalUserId(null), libraryRepository())

        vm.upvoteEvents.test {
            vm.upvote(reaction())
            assertThat(awaitItem()).isEqualTo(ReactionsViewModel.UpvoteEvent.LoginRequired)
        }
    }

    @Test
    fun `upvote does not call repository when not logged in`() = runTest {
        val repository = reactionRepository()
        val vm = ReactionsViewModel(repository, getLocalUserId(null), libraryRepository())

        vm.upvote(reaction())

        verify(repository, never()).upvoteReaction(any(), any())
    }

    @Test
    fun `upvote emits Success with incremented count`() = runTest {
        val repository = reactionRepository {
            onSuspend { upvoteReaction(eq("user-1"), eq("reaction-1")) } doReturn true
        }
        val vm = ReactionsViewModel(repository, getLocalUserId("user-1"), libraryRepository())

        vm.upvoteEvents.test {
            vm.upvote(reaction(upVotesCount = 5))
            val event = awaitItem()
            assertThat(event).isInstanceOf(ReactionsViewModel.UpvoteEvent.Success::class.java)
            event as ReactionsViewModel.UpvoteEvent.Success
            assertThat(event.reactionId).isEqualTo("reaction-1")
            assertThat(event.newCount).isEqualTo(6)
        }
    }

    @Test
    fun `upvote emits Failed when the repository returns false`() = runTest {
        val repository = reactionRepository {
            onSuspend { upvoteReaction(any(), any()) } doReturn false
        }
        val vm = ReactionsViewModel(repository, getLocalUserId("user-1"), libraryRepository())

        vm.upvoteEvents.test {
            vm.upvote(reaction())
            assertThat(awaitItem()).isEqualTo(ReactionsViewModel.UpvoteEvent.Failed)
        }
    }

    @Test
    fun `upvote emits Failed when the repository throws`() = runTest {
        val repository = reactionRepository {
            onSuspend { upvoteReaction(any(), any()) } doThrow RuntimeException("boom")
        }
        val vm = ReactionsViewModel(repository, getLocalUserId("user-1"), libraryRepository())

        useMockedAndroidLogger {
            vm.upvoteEvents.test {
                vm.upvote(reaction())
                assertThat(awaitItem()).isEqualTo(ReactionsViewModel.UpvoteEvent.Failed)
            }
        }
    }
}
