package io.github.drumber.kitsune.ui.search

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.paging.PagingData
import io.github.drumber.kitsune.constants.Defaults
import io.github.drumber.kitsune.constants.Kitsu
import io.github.drumber.kitsune.data.model.ResourceSelector
import io.github.drumber.kitsune.data.model.ResourceType
import io.github.drumber.kitsune.data.model.SearchParams
import io.github.drumber.kitsune.data.model.resource.Resource
import io.github.drumber.kitsune.data.paging.RequestType
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
        return KitsunePref.searchParams.toResourceSelector(
            includeSortFilter = true,
            includeSearchQuery = false
        )
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

    private fun updateResourceSelector(searchParams: SearchParams) {
        val isSearching = isSearching.value == true
        val selector = searchParams.toResourceSelector(
            includeSortFilter = !isSearching,
            includeSearchQuery = isSearching
        )
        setResourceSelector(selector)
    }

    private fun SearchParams.toResourceSelector(
        includeSortFilter: Boolean,
        includeSearchQuery: Boolean
    ): ResourceSelector {
        val params = this
        return ResourceSelector(
            resourceType = params.resourceType,
            requestType = RequestType.ALL,
            filter = Filter().apply {
                if (includeSortFilter) {
                    sort(params.sortOrder.queryParam)
                }
                if (params.categories.isNotEmpty()) {
                    filter("categories", params.categories.joinToString(","))
                }
                if (includeSearchQuery) {
                    searchQuery?.let { filter("text", it) }
                }
            }
        )
    }

    fun updateSearchParams(searchParams: SearchParams) {
        KitsunePref.searchParams = searchParams
        updateResourceSelector(searchParams)
    }

    val searchParams: SearchParams
        get() = KitsunePref.searchParams

    private val _isSearching = MutableLiveData(false)
    val isSearching: LiveData<Boolean>
        get() = _isSearching

    private var searchQuery: String? = null

    fun search(query: String) {
        searchQuery = query
        _isSearching.value = true
        updateResourceSelector(searchParams)
    }

    fun resetSearch() {
        searchQuery = null
        _isSearching.value = false
        updateResourceSelector(searchParams)
    }

    fun restoreDefaultFilter() {
        val defaultSearchParams = Defaults.DEFAULT_SEARCH_PARAMS
        KitsunePref.searchParams = defaultSearchParams
        KitsunePref.searchCategories = emptyList()
        updateResourceSelector(defaultSearchParams)
    }

}