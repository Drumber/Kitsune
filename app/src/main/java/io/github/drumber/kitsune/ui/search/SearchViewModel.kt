package io.github.drumber.kitsune.ui.search

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.paging.PagingData
import io.github.drumber.kitsune.constants.Kitsu
import io.github.drumber.kitsune.data.model.ResourceSelector
import io.github.drumber.kitsune.data.model.ResourceType
import io.github.drumber.kitsune.data.model.resource.Resource
import io.github.drumber.kitsune.data.repository.AnimeRepository
import io.github.drumber.kitsune.data.repository.MangaRepository
import io.github.drumber.kitsune.data.service.Filter
import io.github.drumber.kitsune.preference.KitsunePref
import io.github.drumber.kitsune.ui.base.BaseCollectionViewModel
import kotlinx.coroutines.flow.Flow

class SearchViewModel(
    private val animeRepository: AnimeRepository,
    private val mangaRepository: MangaRepository
) : BaseCollectionViewModel() {

    override fun getStoredResourceSelector(): ResourceSelector {
        return KitsunePref.searchFilter.apply {
            // make sure search text is not stored
            filter.options.remove("filter[text]")
        }
    }

    override fun getData(resourceSelector: ResourceSelector): Flow<PagingData<Resource>> {
        val filter = resourceSelector.filter
        return when (resourceSelector.resourceType) {
            ResourceType.Anime -> animeRepository.animeCollection(
                Kitsu.DEFAULT_PAGE_SIZE,
                filter
            ) as Flow<PagingData<Resource>>
            ResourceType.Manga -> mangaRepository.mangaCollection(
                Kitsu.DEFAULT_PAGE_SIZE,
                filter
            ) as Flow<PagingData<Resource>>
        }
    }

    private val _isSearching = MutableLiveData(false)
    val isSearching: LiveData<Boolean>
        get() = _isSearching

    override fun setResourceSelector(resourceSelector: ResourceSelector) {
        super.setResourceSelector(resourceSelector)
        if(isSearching.value == false) {
            KitsunePref.searchFilter = resourceSelector.copy()
        }
    }

    fun search(query: String) {
        _isSearching.value = true
        val resourceSelector = currentResourceSelector.copy(filter = Filter()
            .filter("text", query))
        setResourceSelector(resourceSelector)
    }

    fun resetSearch() {
        _isSearching.value = false
        setResourceSelector(getStoredResourceSelector())
    }

}