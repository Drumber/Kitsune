package io.github.drumber.kitsune.ui.base

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import io.github.drumber.kitsune.constants.Defaults
import io.github.drumber.kitsune.data.common.Filter
import io.github.drumber.kitsune.data.presentation.model.media.Media
import io.github.drumber.kitsune.data.presentation.model.media.MediaSelector
import io.github.drumber.kitsune.data.presentation.model.media.identifier
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
                started = SharingStarted.Lazily,
                initialValue = null
            )

        setMediaSelector = { mediaSelector ->
            viewModelScope.launch {
                mutableMediaSelectorFlow.emit(mediaSelector)
            }
        }
    }

    val dataSource: Flow<PagingData<out Media>> = mediaSelectorFlow
        .filterNotNull()
        .distinctUntilChanged()
        .flatMapLatest { selector ->
            // copy the filter and limit the fields of the response model to only the required ones
            val mediaSelector = with(selector) {
                copy(
                    filterOptions = Filter(filterOptions.toMutableMap())
                        .fields(mediaType.identifier, *Defaults.MINIMUM_COLLECTION_FIELDS)
                        .options
                )
            }
            getData(mediaSelector)
        }.cachedIn(viewModelScope)

    abstract fun getData(mediaSelector: MediaSelector): Flow<PagingData<Media>>

}