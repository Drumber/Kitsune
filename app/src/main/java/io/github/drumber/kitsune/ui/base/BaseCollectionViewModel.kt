package io.github.drumber.kitsune.ui.base

import androidx.lifecycle.*
import androidx.paging.PagingData
import androidx.paging.cachedIn
import io.github.drumber.kitsune.data.model.ResourceSelector
import io.github.drumber.kitsune.data.model.resource.Resource
import io.github.drumber.kitsune.preference.KitsunePref
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest

abstract class BaseCollectionViewModel: ViewModel() {

    private val _resourceSelector = MutableLiveData(getLastResourceSelector())

    val resourceSelector: LiveData<ResourceSelector>
        get() = _resourceSelector

    fun setResourceSelector(resourceSelector: ResourceSelector, shouldSave: Boolean = true) {
        _resourceSelector.value = resourceSelector
        if(shouldSave) {
            KitsunePref.searchFilter = resourceSelector.copy()
        }
    }

    val currentResourceSelector: ResourceSelector
        get() = resourceSelector.value ?: getLastResourceSelector()

    val dataSource: Flow<PagingData<Resource>> = resourceSelector.asFlow().flatMapLatest { selector ->
        getData(selector)
    }.cachedIn(viewModelScope)

    abstract fun getData(resourceSelector: ResourceSelector): Flow<PagingData<Resource>>

    private fun getLastResourceSelector(): ResourceSelector {
        return KitsunePref.searchFilter
    }

}