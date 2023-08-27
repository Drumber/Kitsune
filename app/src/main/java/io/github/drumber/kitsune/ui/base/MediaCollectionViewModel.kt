package io.github.drumber.kitsune.ui.base

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asFlow
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import io.github.drumber.kitsune.constants.Defaults
import io.github.drumber.kitsune.domain.model.MediaSelector
import io.github.drumber.kitsune.domain.model.infrastructure.media.BaseMedia
import io.github.drumber.kitsune.domain.service.Filter
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest

abstract class MediaCollectionViewModel : ViewModel() {

    private val _mediaSelector = MutableLiveData(getStoredMediaSelector())

    val mediaSelector: LiveData<MediaSelector>
        get() = _mediaSelector

    open fun setMediaSelector(mediaSelector: MediaSelector) {
        if (_mediaSelector.value != mediaSelector) {
            _mediaSelector.value = mediaSelector
        }
    }

    val currentMediaSelector: MediaSelector
        get() = mediaSelector.value ?: getStoredMediaSelector()

    val dataSource: Flow<PagingData<BaseMedia>> = mediaSelector.asFlow()
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

    abstract fun getStoredMediaSelector(): MediaSelector

}