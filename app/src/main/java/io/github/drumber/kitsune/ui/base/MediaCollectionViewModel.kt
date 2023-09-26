package io.github.drumber.kitsune.ui.base

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import io.github.drumber.kitsune.constants.Defaults
import io.github.drumber.kitsune.domain.model.MediaSelector
import io.github.drumber.kitsune.domain.model.infrastructure.media.BaseMedia
import io.github.drumber.kitsune.domain.service.Filter
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

abstract class MediaCollectionViewModel : ViewModel() {

    private val mediaSelectorFlow: StateFlow<MediaSelector?>

    val setMediaSelector: (MediaSelector) -> Unit

    init {
        val mutableMediaSelectorFlow = MutableSharedFlow<MediaSelector>()

        mediaSelectorFlow = mutableMediaSelectorFlow
            .distinctUntilChanged()
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(stopTimeoutMillis = 5000),
                initialValue = null
            )

        setMediaSelector = { mediaSelector ->
            viewModelScope.launch {
                mutableMediaSelectorFlow.emit(mediaSelector)
            }
        }
    }

    val dataSource: Flow<PagingData<out BaseMedia>> = mediaSelectorFlow
        .filterNotNull()
        .distinctUntilChanged()
        .flatMapLatest { selector ->
            // copy the filter and limit the fields of the response model to only the required ones
            val mediaSelector = with(selector) {
                copy(
                    filter = Filter(filter.options.toMutableMap())
                        .fields(mediaType.type, *Defaults.MINIMUM_COLLECTION_FIELDS)
                )
            }
            getData(mediaSelector)
        }.cachedIn(viewModelScope)

    abstract fun getData(mediaSelector: MediaSelector): Flow<PagingData<BaseMedia>>

}