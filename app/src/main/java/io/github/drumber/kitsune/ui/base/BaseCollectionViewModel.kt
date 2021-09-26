package io.github.drumber.kitsune.ui.base

import androidx.lifecycle.*
import androidx.paging.PagingData
import androidx.paging.cachedIn
import io.github.drumber.kitsune.data.model.ResourceSelector
import io.github.drumber.kitsune.data.model.resource.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest

abstract class BaseCollectionViewModel: ViewModel() {

    private val _resourceSelector = MutableLiveData(getStoredResourceSelector())

    val resourceSelector: LiveData<ResourceSelector>
        get() = _resourceSelector

    open fun setResourceSelector(resourceSelector: ResourceSelector) {
        _resourceSelector.value = resourceSelector
    }

    val currentResourceSelector: ResourceSelector
        get() = resourceSelector.value ?: getStoredResourceSelector()

    val dataSource: Flow<PagingData<Resource>> = resourceSelector.asFlow().flatMapLatest { selector ->
        getData(selector)
    }.cachedIn(viewModelScope)

    abstract fun getData(resourceSelector: ResourceSelector): Flow<PagingData<Resource>>

    abstract fun getStoredResourceSelector(): ResourceSelector

}