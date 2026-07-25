package io.github.drumber.kitsune.ui.createpost

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.drumber.kitsune.data.presentation.model.feed.Post
import io.github.drumber.kitsune.data.repository.PostManagementRepository
import io.github.drumber.kitsune.data.repository.UploadRepository
import io.github.drumber.kitsune.domain.user.GetLocalUserIdUseCase
import io.github.drumber.kitsune.util.logE
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.coroutines.cancellation.CancellationException

class CreatePostViewModel(
    private val postManagementRepository: PostManagementRepository,
    private val uploadRepository: UploadRepository,
    private val getLocalUserId: GetLocalUserIdUseCase
) : ViewModel() {

    sealed interface Event {
        data object LoginRequired : Event
        data object Published : Event
        data object Error : Event
    }

    data class UiState(
        val content: String = "",
        val spoiler: Boolean = false,
        val nsfw: Boolean = false,
        val isPublishing: Boolean = false,
        val isEditMode: Boolean = false,
        val media: SelectedMedia? = null,
        val unit: SelectedUnit? = null,
        val images: List<SelectedImage> = emptyList(),
        /** Name of the user whose wall this post targets, or `null` for a regular post. */
        val wallTargetName: String? = null,
        /** Name of the group this post targets, or `null` for a regular post. */
        val groupTargetName: String? = null
    ) {
        val canPublish: Boolean
            get() = !isPublishing && (content.isNotBlank() || images.isNotEmpty())
    }

    data class SelectedMedia(
        val id: String,
        val title: String,
        val posterUrl: String?,
        val isAnime: Boolean
    )

    data class SelectedUnit(
        val id: String,
        val number: Int,
        val title: String,
        val isEpisode: Boolean
    )

    data class SelectedImage(
        val uri: String,
        val dataUri: String? = null,
        val existingUploadId: String? = null
    )

    private val _uiState = MutableStateFlow(UiState())
    val uiState = _uiState.asStateFlow()

    /** Id of the post being edited, or `null` when composing a new post. */
    private var editPostId: String? = null

    /** Id of the user whose wall the new post targets, or `null` for a regular post. */
    private var targetUserId: String? = null

    /** Id of the group the new post targets, or `null` for a regular post. */
    private var targetGroupId: String? = null

    private val eventChannel = Channel<Event>(Channel.BUFFERED)
    val events: Flow<Event> = eventChannel.receiveAsFlow()

    /**
     * Fetches a post by its id and pre-fills the composer for editing. Used by the Compose
     * navigation graph, which passes only the id via [Routes.CreatePost.editPostId].
     */
    fun initFromPostId(postId: String) {
        if (editPostId != null) return
        viewModelScope.launch {
            try {
                postManagementRepository.getPost(postId)?.let { initFromPost(it) }
            } catch (e: Exception) {
                logE("Failed to load post '$postId' for editing.", e)
            }
        }
    }

    /** Prefills the composer from an existing post for editing. Called once on screen creation. */
    fun initFromPost(post: Post) {
        if (editPostId != null) return
        editPostId = post.id
        _uiState.value = UiState(
            content = post.content ?: "",
            spoiler = post.spoiler,
            nsfw = post.nsfw,
            isEditMode = true,
            media = if (post.mediaId != null) {
                SelectedMedia(
                    id = post.mediaId,
                    title = post.mediaTitle ?: "",
                    posterUrl = post.mediaPosterUrl,
                    isAnime = post.mediaIsAnime ?: true
                )
            } else {
                null
            },
            unit = if (post.spoiledUnitId != null) {
                SelectedUnit(
                    id = post.spoiledUnitId,
                    number = post.spoiledUnitNumber ?: 0,
                    title = post.spoiledUnitTitle ?: "",
                    isEpisode = post.spoiledUnitIsEpisode ?: true
                )
            } else {
                null
            },
            images = post.uploadIds.mapIndexed { index, id ->
                SelectedImage(
                    uri = post.imageUrls.getOrNull(index) ?: id,
                    existingUploadId = id
                )
            }
        )
    }

    fun setContent(content: String) {
        _uiState.update { it.copy(content = content) }
    }

    /** Marks this composer as a wall post targeting the given user. Called once on creation. */
    fun setWallTarget(userId: String, userName: String?) {
        if (targetUserId != null) return
        targetUserId = userId
        _uiState.update { it.copy(wallTargetName = userName) }
    }

    /** Marks this composer as a post targeting the given group. Called once on creation. */
    fun setGroupTarget(groupId: String, groupName: String?) {
        if (targetGroupId != null) return
        targetGroupId = groupId
        _uiState.update { it.copy(groupTargetName = groupName) }
    }
    fun setSpoiler(spoiler: Boolean) {
        _uiState.update { it.copy(spoiler = spoiler) }
    }

    fun setNsfw(nsfw: Boolean) {
        _uiState.update { it.copy(nsfw = nsfw) }
    }

    fun setMedia(media: SelectedMedia) {
        // Changing the tagged media invalidates a previously selected unit.
        _uiState.update { it.copy(media = media, unit = null) }
    }

    fun clearMedia() {
        _uiState.update { it.copy(media = null, unit = null) }
    }

    fun setUnit(unit: SelectedUnit) {
        _uiState.update { it.copy(unit = unit) }
    }

    fun clearUnit() {
        _uiState.update { it.copy(unit = null) }
    }

    fun addImage(uri: String, dataUri: String) {
        _uiState.update { state ->
            if (state.images.size >= MAX_IMAGES) return@update state
            state.copy(images = state.images + SelectedImage(uri = uri, dataUri = dataUri))
        }
    }

    fun removeImage(index: Int) {
        _uiState.update { state ->
            if (index !in state.images.indices) return@update state
            state.copy(images = state.images.filterIndexed { i, _ -> i != index })
        }
    }

    fun reorderImages(orderedUris: List<String>) {
        _uiState.update { state ->
            val current = state.images.toMutableList()
            val reordered = orderedUris.mapNotNull { uri ->
                val index = current.indexOfFirst { it.uri == uri }
                if (index >= 0) current.removeAt(index) else null
            }
            if (reordered.size == state.images.size) {
                state.copy(images = reordered)
            } else {
                state
            }
        }
    }

    fun publish() {
        val state = _uiState.value
        if (state.isPublishing) return

        val content = state.content.trim()
        if (content.isEmpty() && state.images.isEmpty()) return

        val userId = getLocalUserId()
        if (userId == null) {
            eventChannel.trySend(Event.LoginRequired)
            return
        }

        val mediaState = state.media
        val unitState = state.unit

        _uiState.update { it.copy(isPublishing = true) }
        viewModelScope.launch {
            try {
                val uploadIds = state.images.mapIndexed { index, image ->
                    image.existingUploadId
                        ?: uploadRepository.uploadImage(
                            userId,
                            image.dataUri ?: throw IllegalStateException("Missing image data."),
                            index
                        )
                        ?: throw IllegalStateException("Image upload returned no id.")
                }
                val editId = editPostId
                val post = if (editId != null) {
                    postManagementRepository.updatePost(
                        postId = editId,
                        content = content,
                        spoiler = state.spoiler,
                        nsfw = state.nsfw,
                        mediaId = mediaState?.id,
                        mediaIsAnime = mediaState?.isAnime ?: false,
                        spoiledUnitId = unitState?.id,
                        spoiledUnitIsEpisode = unitState?.isEpisode ?: false,
                        uploadIds = uploadIds
                    )
                } else {
                    postManagementRepository.postPost(
                        userId = userId,
                        content = content,
                        spoiler = state.spoiler,
                        nsfw = state.nsfw,
                        mediaId = mediaState?.id,
                        mediaIsAnime = mediaState?.isAnime ?: false,
                        spoiledUnitId = unitState?.id,
                        spoiledUnitIsEpisode = unitState?.isEpisode ?: false,
                        uploadIds = uploadIds,
                        targetUserId = targetUserId,
                        targetGroupId = targetGroupId
                    )
                }
                if (post != null) {
                    eventChannel.send(Event.Published)
                } else {
                    _uiState.update { it.copy(isPublishing = false) }
                    eventChannel.send(Event.Error)
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                logE("Failed to publish post.", e)
                _uiState.update { it.copy(isPublishing = false) }
                eventChannel.send(Event.Error)
            }
        }
    }

    companion object {
        const val MAX_IMAGES = 10
    }
}
