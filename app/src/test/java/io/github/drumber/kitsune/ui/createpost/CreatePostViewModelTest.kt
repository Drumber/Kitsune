package io.github.drumber.kitsune.ui.createpost

import io.github.drumber.kitsune.data.presentation.model.feed.Post
import io.github.drumber.kitsune.data.repository.PostManagementRepository
import io.github.drumber.kitsune.data.repository.UploadRepository
import io.github.drumber.kitsune.data.repository.UserRepository
import io.github.drumber.kitsune.domain.user.GetLocalUserIdUseCase
import io.github.drumber.kitsune.testutils.MainDispatcherRule
import io.github.drumber.kitsune.testutils.onSuspend
import io.github.drumber.kitsune.testutils.useMockedAndroidLogger
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.anyOrNull
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.doThrow
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoInteractions

@OptIn(ExperimentalCoroutinesApi::class)
class CreatePostViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private fun viewModel(
        postManagementRepository: PostManagementRepository = mock(),
        uploadRepository: UploadRepository = mock(),
        getLocalUserId: GetLocalUserIdUseCase = mock { on { invoke() } doReturn "user-1" },
        userRepository: UserRepository = mock { on { localUser } doReturn MutableStateFlow(null) }
    ) = CreatePostViewModel(postManagementRepository, uploadRepository, getLocalUserId, userRepository)

    @Test
    fun `initial state is empty and cannot publish`() {
        val state = viewModel().uiState.value

        assertThat(state.content).isEmpty()
        assertThat(state.spoiler).isFalse()
        assertThat(state.nsfw).isFalse()
        assertThat(state.isPublishing).isFalse()
        assertThat(state.isEditMode).isFalse()
        assertThat(state.images).isEmpty()
        assertThat(state.canPublish).isFalse()
    }

    @Test
    fun `setContent makes the post publishable`() {
        val vm = viewModel()

        vm.setContent("Hello world")

        assertThat(vm.uiState.value.content).isEqualTo("Hello world")
        assertThat(vm.uiState.value.canPublish).isTrue()
    }

    @Test
    fun `blank content with images is publishable`() {
        val vm = viewModel()

        vm.addImage(uri = "content://1", dataUri = "data:1")

        assertThat(vm.uiState.value.content).isBlank()
        assertThat(vm.uiState.value.canPublish).isTrue()
    }

    @Test
    fun `setMedia clears a previously selected unit`() {
        val vm = viewModel()
        vm.setMedia(CreatePostViewModel.SelectedMedia("m1", "Title", null, isAnime = true))
        vm.setUnit(CreatePostViewModel.SelectedUnit("u1", 1, "Ep 1", isEpisode = true))

        vm.setMedia(CreatePostViewModel.SelectedMedia("m2", "Other", null, isAnime = false))

        assertThat(vm.uiState.value.media?.id).isEqualTo("m2")
        assertThat(vm.uiState.value.unit).isNull()
    }

    @Test
    fun `clearMedia also clears the unit`() {
        val vm = viewModel()
        vm.setMedia(CreatePostViewModel.SelectedMedia("m1", "Title", null, isAnime = true))
        vm.setUnit(CreatePostViewModel.SelectedUnit("u1", 1, "Ep 1", isEpisode = true))

        vm.clearMedia()

        assertThat(vm.uiState.value.media).isNull()
        assertThat(vm.uiState.value.unit).isNull()
    }

    @Test
    fun `addImage respects the maximum image count`() {
        val vm = viewModel()

        repeat(CreatePostViewModel.MAX_IMAGES + 5) { index ->
            vm.addImage(uri = "content://$index", dataUri = "data:$index")
        }

        assertThat(vm.uiState.value.images).hasSize(CreatePostViewModel.MAX_IMAGES)
    }

    @Test
    fun `removeImage removes the image at the given index`() {
        val vm = viewModel()
        vm.addImage("content://0", "data:0")
        vm.addImage("content://1", "data:1")

        vm.removeImage(0)

        assertThat(vm.uiState.value.images.map { it.uri }).containsExactly("content://1")
    }

    @Test
    fun `removeImage ignores out-of-bounds index`() {
        val vm = viewModel()
        vm.addImage("content://0", "data:0")

        vm.removeImage(5)

        assertThat(vm.uiState.value.images).hasSize(1)
    }

    @Test
    fun `reorderImages reorders images by uri`() {
        val vm = viewModel()
        vm.addImage("content://a", "data:a")
        vm.addImage("content://b", "data:b")
        vm.addImage("content://c", "data:c")

        vm.reorderImages(listOf("content://c", "content://a", "content://b"))

        assertThat(vm.uiState.value.images.map { it.uri })
            .containsExactly("content://c", "content://a", "content://b")
    }

    @Test
    fun `reorderImages ignores incomplete uri lists`() {
        val vm = viewModel()
        vm.addImage("content://a", "data:a")
        vm.addImage("content://b", "data:b")

        vm.reorderImages(listOf("content://b"))

        assertThat(vm.uiState.value.images.map { it.uri }).containsExactly("content://a", "content://b")
    }

    @Test
    fun `initFromPost prefills the composer in edit mode`() {
        val vm = viewModel()

        vm.initFromPost(samplePost())

        val state = vm.uiState.value
        assertThat(state.isEditMode).isTrue()
        assertThat(state.content).isEqualTo("Original content")
        assertThat(state.spoiler).isTrue()
        assertThat(state.nsfw).isTrue()
        assertThat(state.media?.id).isEqualTo("media-1")
        assertThat(state.unit?.id).isEqualTo("unit-1")
        assertThat(state.images.map { it.existingUploadId }).containsExactly("up-1", "up-2")
    }

    @Test
    fun `initFromPost is only applied once`() {
        val vm = viewModel()

        vm.initFromPost(samplePost())
        vm.initFromPost(samplePost().copy(id = "other", content = "Changed"))

        assertThat(vm.uiState.value.content).isEqualTo("Original content")
    }

    @Test
    fun `publish without a logged-in user emits LoginRequired`() = runTest {
        val getLocalUserId = mock<GetLocalUserIdUseCase> { on { invoke() } doReturn null }
        val postManagementRepository = mock<PostManagementRepository>()
        val vm = viewModel(
            postManagementRepository = postManagementRepository,
            getLocalUserId = getLocalUserId
        )
        vm.setContent("Some content")

        vm.publish()

        assertThat(vm.events.first()).isEqualTo(CreatePostViewModel.Event.LoginRequired)
        assertThat(vm.uiState.value.isPublishing).isFalse()
        verifyNoInteractions(postManagementRepository)
    }

    @Test
    fun `publish does nothing for blank content without images`() {
        val postManagementRepository = mock<PostManagementRepository>()
        val vm = viewModel(postManagementRepository = postManagementRepository)

        vm.publish()

        verifyNoInteractions(postManagementRepository)
        assertThat(vm.uiState.value.isPublishing).isFalse()
    }

    @Test
    fun `publish creates a new post and emits Published`() = runTest {
        val postManagementRepository = mock<PostManagementRepository> {
            onSuspend {
                postPost(
                    userId = any(),
                    content = anyOrNull(),
                    spoiler = any(),
                    nsfw = any(),
                    mediaId = anyOrNull(),
                    mediaIsAnime = any(),
                    spoiledUnitId = anyOrNull(),
                    spoiledUnitIsEpisode = any(),
                    uploadIds = any(),
                    targetUserId = anyOrNull(),
                    targetGroupId = anyOrNull()
                )
            } doReturn samplePost()
        }
        val vm = viewModel(postManagementRepository = postManagementRepository)
        vm.setContent("New post")

        vm.publish()

        assertThat(vm.events.first()).isEqualTo(CreatePostViewModel.Event.Published)
        verify(postManagementRepository).postPost(
            userId = eq("user-1"),
            content = eq("New post"),
            spoiler = any(),
            nsfw = any(),
            mediaId = anyOrNull(),
            mediaIsAnime = any(),
            spoiledUnitId = anyOrNull(),
            spoiledUnitIsEpisode = any(),
            uploadIds = any(),
            targetUserId = anyOrNull(),
            targetGroupId = anyOrNull()
        )
    }

    @Test
    fun `publish in edit mode updates the post`() = runTest {
        val postManagementRepository = mock<PostManagementRepository> {
            onSuspend {
                updatePost(
                    postId = any(),
                    content = anyOrNull(),
                    spoiler = any(),
                    nsfw = any(),
                    mediaId = anyOrNull(),
                    mediaIsAnime = any(),
                    spoiledUnitId = anyOrNull(),
                    spoiledUnitIsEpisode = any(),
                    uploadIds = any()
                )
            } doReturn samplePost()
        }
        val vm = viewModel(postManagementRepository = postManagementRepository)
        vm.initFromPost(samplePost())
        vm.setContent("Edited content")

        vm.publish()

        assertThat(vm.events.first()).isEqualTo(CreatePostViewModel.Event.Published)
        verify(postManagementRepository).updatePost(
            postId = eq("post-1"),
            content = eq("Edited content"),
            spoiler = any(),
            nsfw = any(),
            mediaId = anyOrNull(),
            mediaIsAnime = any(),
            spoiledUnitId = anyOrNull(),
            spoiledUnitIsEpisode = any(),
            uploadIds = any()
        )
        verify(postManagementRepository, never()).postPost(
            userId = any(),
            content = anyOrNull(),
            spoiler = any(),
            nsfw = any(),
            mediaId = anyOrNull(),
            mediaIsAnime = any(),
            spoiledUnitId = anyOrNull(),
            spoiledUnitIsEpisode = any(),
            uploadIds = any(),
            targetUserId = anyOrNull(),
            targetGroupId = anyOrNull()
        )
    }

    @Test
    fun `publish emits Error and resets publishing when the server returns null`() = runTest {
        val postManagementRepository = mock<PostManagementRepository> {
            onSuspend {
                postPost(
                    userId = any(),
                    content = anyOrNull(),
                    spoiler = any(),
                    nsfw = any(),
                    mediaId = anyOrNull(),
                    mediaIsAnime = any(),
                    spoiledUnitId = anyOrNull(),
                    spoiledUnitIsEpisode = any(),
                    uploadIds = any(),
                    targetUserId = anyOrNull(),
                    targetGroupId = anyOrNull()
                )
            } doReturn null
        }
        val vm = viewModel(postManagementRepository = postManagementRepository)
        vm.setContent("New post")

        vm.publish()

        assertThat(vm.events.first()).isEqualTo(CreatePostViewModel.Event.Error)
        assertThat(vm.uiState.value.isPublishing).isFalse()
    }

    @Test
    fun `publish emits Error and resets publishing when the repository throws`() = runTest {
        val postManagementRepository = mock<PostManagementRepository> {
            onSuspend {
                postPost(
                    userId = any(),
                    content = anyOrNull(),
                    spoiler = any(),
                    nsfw = any(),
                    mediaId = anyOrNull(),
                    mediaIsAnime = any(),
                    spoiledUnitId = anyOrNull(),
                    spoiledUnitIsEpisode = any(),
                    uploadIds = any(),
                    targetUserId = anyOrNull(),
                    targetGroupId = anyOrNull()
                )
            } doThrow RuntimeException("network down")
        }
        val vm = viewModel(postManagementRepository = postManagementRepository)
        vm.setContent("New post")

        useMockedAndroidLogger { vm.publish() }

        assertThat(vm.events.first()).isEqualTo(CreatePostViewModel.Event.Error)
        assertThat(vm.uiState.value.isPublishing).isFalse()
    }

    private fun samplePost() = Post(
        id = "post-1",
        createdAt = null,
        content = "Original content",
        contentFormatted = null,
        spoiler = true,
        nsfw = true,
        commentsCount = 0,
        likesCount = 0,
        authorId = "user-1",
        authorName = "Author",
        authorAvatarUrl = null,
        mediaTitle = "Media title",
        mediaId = "media-1",
        mediaPosterUrl = null,
        mediaSynopsis = null,
        mediaSlug = null,
        mediaIsAnime = true,
        spoiledUnitNumber = 1,
        spoiledUnitId = "unit-1",
        spoiledUnitTitle = "Episode 1",
        spoiledUnitIsEpisode = true,
        imageUrls = listOf("https://img/1", "https://img/2"),
        uploadIds = listOf("up-1", "up-2"),
        embed = null
    )
}
