package io.github.drumber.kitsune.ui.main

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asFlow
import androidx.lifecycle.viewModelScope
import androidx.paging.cachedIn
import io.github.drumber.kitsune.constants.Kitsu
import io.github.drumber.kitsune.constants.SortFilter
import io.github.drumber.kitsune.constants.SortFilter.desc
import io.github.drumber.kitsune.data.repository.AnimeRepository
import io.github.drumber.kitsune.data.service.Filter
import kotlinx.coroutines.flow.flatMapLatest

class MainFragmentViewModel(
    private val repository: AnimeRepository
) : ViewModel() {

    private val currentFilter = MutableLiveData(getLastFilter())

    val anime = currentFilter.asFlow().flatMapLatest { filter ->
        repository.animeCollection(Kitsu.DEFAULT_PAGE_SIZE, filter)
    }.cachedIn(viewModelScope)

    fun setFilter(filter: Filter) {
        currentFilter.value = filter
    }

    private fun getLastFilter(): Filter {
        // TODO: get last used filter from storage
        return Filter().sort(SortFilter.POPULARITY.desc())
    }

}