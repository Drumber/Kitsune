package io.github.drumber.kitsune.ui.feed

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import io.github.drumber.kitsune.data.presentation.model.feed.Post
import io.github.drumber.kitsune.data.repository.FeedRepository
import io.github.drumber.kitsune.domain.user.GetLocalUserIdUseCase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map

@OptIn(ExperimentalCoroutinesApi::class)
class FeedListViewModel(
    private val feedRepository: FeedRepository,
    private val getLocalUserId: GetLocalUserIdUseCase
) : ViewModel() {

    private val feedType = MutableStateFlow<FeedType?>(null)

    fun setFeedType(type: FeedType) {
        if (feedType.value != type) {
            feedType.value = type
        }
    }

    /** Emits `true` if the current feed requires the user to be logged in but they are not. */
    val loginRequired: Flow<Boolean> = feedType.filterNotNull().map { type ->
        type == FeedType.FOLLOWING && getLocalUserId() == null
    }

    val dataSource: Flow<PagingData<Post>> = feedType.filterNotNull().flatMapLatest { type ->
        when (type) {
            FeedType.GLOBAL -> feedRepository.globalFeedPager()
            FeedType.FOLLOWING -> {
                val userId = getLocalUserId()
                if (userId == null) {
                    flowOf(PagingData.empty())
                } else {
                    feedRepository.timelineFeedPager(userId)
                }
            }
        }
    }.cachedIn(viewModelScope)

}
