package io.github.drumber.kitsune.ui.base

import androidx.lifecycle.*
import androidx.paging.PagingData
import androidx.paging.cachedIn
import io.github.drumber.kitsune.constants.SortFilter
import io.github.drumber.kitsune.constants.SortFilter.desc
import io.github.drumber.kitsune.data.model.ResourceSelector
import io.github.drumber.kitsune.data.model.ResourceType
import io.github.drumber.kitsune.data.model.resource.Resource
import io.github.drumber.kitsune.data.service.Filter
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest

abstract class BaseCollectionViewModel: ViewModel() {

    private val _resourceSelector = MutableLiveData(getLastResourceSelector())

    val resourceSelector: LiveData<ResourceSelector>
        get() = _resourceSelector

    fun setResourceSelector(resourceSelector: ResourceSelector) {
        _resourceSelector.value = resourceSelector
    }

    val dataSource: Flow<PagingData<Resource>> = resourceSelector.asFlow().flatMapLatest { selector ->
        getData(selector)
    }.cachedIn(viewModelScope)

    abstract fun getData(resourceSelector: ResourceSelector): Flow<PagingData<Resource>>

    private fun getLastResourceSelector(): ResourceSelector {
        // TODO: get last used selector from preferences
        return DEFAULT_RESOURCE_SELECTOR
    }

    companion object {
        val DEFAULT_FILTER = Filter().sort(SortFilter.POPULARITY.desc())
        val DEFAULT_RESOURCE_SELECTOR = ResourceSelector(ResourceType.Anime, DEFAULT_FILTER)
    }

}