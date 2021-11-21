package io.github.drumber.kitsune.ui.base

import androidx.lifecycle.*
import androidx.paging.PagingData
import androidx.paging.cachedIn
import io.github.drumber.kitsune.constants.Defaults
import io.github.drumber.kitsune.data.model.ResourceSelector
import io.github.drumber.kitsune.data.model.resource.Resource
import io.github.drumber.kitsune.data.service.Filter
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest

abstract class ResourceCollectionViewModel : ViewModel() {

    private val _resourceSelector = MutableLiveData(getStoredResourceSelector())

    val resourceSelector: LiveData<ResourceSelector>
        get() = _resourceSelector

    open fun setResourceSelector(resourceSelector: ResourceSelector) {
        _resourceSelector.value = resourceSelector
    }

    val currentResourceSelector: ResourceSelector
        get() = resourceSelector.value ?: getStoredResourceSelector()

    val dataSource: Flow<PagingData<Resource>> = resourceSelector.asFlow()
        .flatMapLatest { selector ->
            // copy the filter and limit the fields of the response model to only the required ones
            val resourceSelector = with(selector) {
                copy(
                    filter = Filter(filter.options.toMutableMap())
                        .fields(resourceType.type, *Defaults.MINIMUM_COLLECTION_FIELDS)
                )
            }
            getData(resourceSelector)
        }.cachedIn(viewModelScope)

    abstract fun getData(resourceSelector: ResourceSelector): Flow<PagingData<Resource>>

    abstract fun getStoredResourceSelector(): ResourceSelector

}