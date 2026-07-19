package io.github.drumber.kitsune.ui.details.feed

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import io.github.drumber.kitsune.data.presentation.model.feed.Post
import io.github.drumber.kitsune.data.repository.FeedRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest

@OptIn(ExperimentalCoroutinesApi::class)
class MediaFeedViewModel(
    private val feedRepository: FeedRepository
) : ViewModel() {

    private data class MediaKey(val mediaId: String, val isAnime: Boolean)

    private val mediaKey = MutableStateFlow<MediaKey?>(null)

    fun setMedia(mediaId: String, isAnime: Boolean) {
        val key = MediaKey(mediaId, isAnime)
        if (mediaKey.value != key) {
            mediaKey.value = key
        }
    }

    val dataSource: Flow<PagingData<Post>> = mediaKey.filterNotNull().flatMapLatest { key ->
        feedRepository.mediaFeedPager(key.isAnime, key.mediaId)
    }.cachedIn(viewModelScope)

}
