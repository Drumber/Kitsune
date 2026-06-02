package io.github.drumber.kitsune.ui.createpost

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.drumber.kitsune.data.repository.PostManagementRepository
import io.github.drumber.kitsune.data.repository.UploadRepository
import io.github.drumber.kitsune.data.source.network.media.model.NetworkAnime
import io.github.drumber.kitsune.data.source.network.media.model.NetworkManga
import io.github.drumber.kitsune.data.source.network.media.model.NetworkMedia
import io.github.drumber.kitsune.data.source.network.media.model.unit.NetworkChapter
import io.github.drumber.kitsune.data.source.network.media.model.unit.NetworkEpisode
import io.github.drumber.kitsune.data.source.network.media.model.unit.NetworkMediaUnit
import io.github.drumber.kitsune.domain.user.GetLocalUserIdUseCase
import io.github.drumber.kitsune.util.logE
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

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
        val media: SelectedMedia? = null,
        val unit: SelectedUnit? = null,
        val images: List<SelectedImage> = emptyList()
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
        val dataUri: String
    )

    private val _uiState = MutableStateFlow(UiState())
    val uiState = _uiState.asStateFlow()

    private val eventChannel = Channel<Event>(Channel.BUFFERED)
    val events: Flow<Event> = eventChannel.receiveAsFlow()

    fun setContent(content: String) {
        _uiState.value = _uiState.value.copy(content = content)
    }

    fun setSpoiler(spoiler: Boolean) {
        _uiState.value = _uiState.value.copy(spoiler = spoiler)
    }

    fun setNsfw(nsfw: Boolean) {
        _uiState.value = _uiState.value.copy(nsfw = nsfw)
    }

    fun setMedia(media: SelectedMedia) {
        // Changing the tagged media invalidates a previously selected unit.
        _uiState.value = _uiState.value.copy(media = media, unit = null)
    }

    fun clearMedia() {
        _uiState.value = _uiState.value.copy(media = null, unit = null)
    }

    fun setUnit(unit: SelectedUnit) {
        _uiState.value = _uiState.value.copy(unit = unit)
    }

    fun clearUnit() {
        _uiState.value = _uiState.value.copy(unit = null)
    }

    fun addImage(uri: String, dataUri: String) {
        if (_uiState.value.images.size >= MAX_IMAGES) return
        _uiState.value = _uiState.value.copy(images = _uiState.value.images + SelectedImage(uri, dataUri))
    }

    fun removeImage(index: Int) {
        val images = _uiState.value.images
        if (index !in images.indices) return
        _uiState.value = _uiState.value.copy(images = images.filterIndexed { i, _ -> i != index })
    }

    fun reorderImages(orderedUris: List<String>) {
        val current = _uiState.value.images.toMutableList()
        val reordered = orderedUris.mapNotNull { uri ->
            val index = current.indexOfFirst { it.uri == uri }
            if (index >= 0) current.removeAt(index) else null
        }
        if (reordered.size == _uiState.value.images.size) {
            _uiState.value = _uiState.value.copy(images = reordered)
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

        val mediaStub: NetworkMedia? = state.media?.let { media ->
            if (media.isAnime) NetworkAnime.empty(media.id) else NetworkManga.empty(media.id)
        }
        val unitStub: NetworkMediaUnit? = state.unit?.let { unit ->
            if (unit.isEpisode) NetworkEpisode.empty(unit.id) else NetworkChapter.empty(unit.id)
        }

        _uiState.value = state.copy(isPublishing = true)
        viewModelScope.launch {
            try {
                val uploadIds = state.images.mapIndexed { index, image ->
                    uploadRepository.uploadImage(userId, image.dataUri, index)
                        ?: throw IllegalStateException("Image upload returned no id.")
                }
                val post = postManagementRepository.postPost(
                    userId = userId,
                    content = content,
                    spoiler = state.spoiler,
                    nsfw = state.nsfw,
                    media = mediaStub,
                    spoiledUnit = unitStub,
                    uploadIds = uploadIds
                )
                if (post != null) {
                    eventChannel.send(Event.Published)
                } else {
                    _uiState.value = _uiState.value.copy(isPublishing = false)
                    eventChannel.send(Event.Error)
                }
            } catch (e: Exception) {
                logE("Failed to publish post.", e)
                _uiState.value = _uiState.value.copy(isPublishing = false)
                eventChannel.send(Event.Error)
            }
        }
    }

    companion object {
        const val MAX_IMAGES = 10
    }

}
