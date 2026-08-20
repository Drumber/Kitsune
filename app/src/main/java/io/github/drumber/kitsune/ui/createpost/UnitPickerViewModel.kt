package io.github.drumber.kitsune.ui.createpost

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import io.github.drumber.kitsune.config.Kitsu
import io.github.drumber.kitsune.data.common.Filter
import io.github.drumber.kitsune.data.presentation.model.media.unit.MediaUnit
import io.github.drumber.kitsune.data.repository.MediaUnitRepository
import io.github.drumber.kitsune.data.repository.MediaUnitRepository.MediaUnitType
import kotlinx.coroutines.flow.Flow

class UnitPickerViewModel(
    private val mediaUnitRepository: MediaUnitRepository
) : ViewModel() {

    private var dataSourceFlow: Flow<PagingData<MediaUnit>>? = null

    /** Returns a pager for the given media's episodes (anime) or chapters (manga), ordered by number. */
    fun unitPager(mediaId: String, isAnime: Boolean): Flow<PagingData<MediaUnit>> {
        dataSourceFlow?.let { return it }

        val filter = Filter().sort("number")
        val type = if (isAnime) {
            filter.filter("media_id", mediaId)
            MediaUnitType.EPISODE
        } else {
            filter.filter("manga_id", mediaId)
            MediaUnitType.CHAPTER
        }

        return mediaUnitRepository.mediaUnitPager(type, filter, Kitsu.DEFAULT_PAGE_SIZE)
            .cachedIn(viewModelScope)
            .also { dataSourceFlow = it }
    }
}
