package io.github.drumber.kitsune.ui.details

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import io.github.drumber.kitsune.data.presentation.model.library.LibraryStatus
import io.github.drumber.kitsune.data.presentation.model.reaction.MediaReaction
import io.github.drumber.kitsune.data.repository.AnimeRepository
import io.github.drumber.kitsune.data.repository.FavoriteRepository
import io.github.drumber.kitsune.data.repository.LibraryRepository
import io.github.drumber.kitsune.data.repository.MangaRepository
import io.github.drumber.kitsune.data.repository.MappingRepository
import io.github.drumber.kitsune.data.repository.MediaReactionRepository
import io.github.drumber.kitsune.domain.auth.IsUserLoggedInUseCase
import io.github.drumber.kitsune.domain.library.UpdateLibraryEntryUseCase
import io.github.drumber.kitsune.domain.user.GetLocalUserIdUseCase
import io.github.drumber.kitsune.testutils.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.verifyNoInteractions

@OptIn(ExperimentalCoroutinesApi::class)
class DetailsViewModelTest {

    @get:Rule
    val instantTaskRule = InstantTaskExecutorRule()

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private fun viewModel(
        getLocalUserId: GetLocalUserIdUseCase = mock(),
        isUserLoggedIn: IsUserLoggedInUseCase = mock(),
        updateLibraryEntry: UpdateLibraryEntryUseCase = mock(),
        favoriteRepository: FavoriteRepository = mock(),
        libraryRepository: LibraryRepository = mock(),
        animeRepository: AnimeRepository = mock(),
        mangaRepository: MangaRepository = mock(),
        mappingRepository: MappingRepository = mock(),
        mediaReactionRepository: MediaReactionRepository = mock()
    ) = DetailsViewModel(
        getLocalUserId,
        isUserLoggedIn,
        updateLibraryEntry,
        favoriteRepository,
        libraryRepository,
        animeRepository,
        mangaRepository,
        mappingRepository,
        mediaReactionRepository
    )

    @Test
    fun `isLoggedIn returns true when the use case reports a logged-in user`() {
        val vm = viewModel(isUserLoggedIn = mock { on { invoke() } doReturn true })

        assertThat(vm.isLoggedIn()).isTrue()
    }

    @Test
    fun `isLoggedIn returns false when the use case reports no user`() {
        val vm = viewModel(isUserLoggedIn = mock { on { invoke() } doReturn false })

        assertThat(vm.isLoggedIn()).isFalse()
    }

    @Test
    fun `initial state exposes empty media, no loading and initial mappings`() {
        val vm = viewModel()

        assertThat(vm.mediaModel.value).isNull()
        assertThat(vm.libraryEntryWrapper.value).isNull()
        assertThat(vm.isLoading.value).isFalse()
        assertThat(vm.mappingsSate.value).isEqualTo(MediaMappingsSate.Initial)
        assertThat(vm.areAllTileLanguagesShown).isFalse()
    }

    @Test
    fun `updateLibraryEntryStatus does nothing when there is no local user`() {
        val libraryRepository = mock<LibraryRepository>()
        val updateLibraryEntry = mock<UpdateLibraryEntryUseCase>()
        val vm = viewModel(
            getLocalUserId = mock { on { invoke() } doReturn null },
            libraryRepository = libraryRepository,
            updateLibraryEntry = updateLibraryEntry
        )

        vm.updateLibraryEntryStatus(LibraryStatus.Current)

        verifyNoInteractions(libraryRepository)
        verifyNoInteractions(updateLibraryEntry)
    }

    @Test
    fun `removeLibraryEntry does nothing when there is no library entry`() {
        val libraryRepository = mock<LibraryRepository>()
        val vm = viewModel(libraryRepository = libraryRepository)

        vm.removeLibraryEntry()

        verifyNoInteractions(libraryRepository)
    }

    @Test
    fun `loadMappingsIfNotAlreadyLoaded does nothing without a media model`() {
        val mappingRepository = mock<MappingRepository>()
        val vm = viewModel(mappingRepository = mappingRepository)

        vm.loadMappingsIfNotAlreadyLoaded()

        assertThat(vm.mappingsSate.value).isEqualTo(MediaMappingsSate.Initial)
        verifyNoInteractions(mappingRepository)
    }

    @Test
    fun `upvoteReaction emits LoginRequired when there is no local user`() = runTest {
        val vm = viewModel(getLocalUserId = mock { on { invoke() } doReturn null })

        vm.upvoteReaction(mock<MediaReaction>())

        assertThat(vm.reactionUpvoteEvents.first()).isEqualTo(ReactionUpvoteEvent.LoginRequired)
    }
}
