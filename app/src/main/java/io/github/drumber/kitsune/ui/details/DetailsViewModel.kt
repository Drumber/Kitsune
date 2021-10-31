package io.github.drumber.kitsune.ui.details

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import io.github.drumber.kitsune.data.model.resource.ResourceAdapter

class DetailsViewModel : ViewModel() {

    private val _resourceAdapter = MutableLiveData<ResourceAdapter>()
    val resourceAdapter: LiveData<ResourceAdapter>
        get() = _resourceAdapter

    fun initResourceAdapter(resourceAdapter: ResourceAdapter) {
        if (_resourceAdapter.value == null) {
            _resourceAdapter.value = resourceAdapter
        }
    }

}