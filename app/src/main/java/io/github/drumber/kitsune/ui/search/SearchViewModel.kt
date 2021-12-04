package io.github.drumber.kitsune.ui.search

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.paging.PagingData
import io.github.drumber.kitsune.constants.Defaults
import io.github.drumber.kitsune.constants.Kitsu
import io.github.drumber.kitsune.data.model.MediaSelector
import io.github.drumber.kitsune.data.model.MediaType
import io.github.drumber.kitsune.data.model.SearchParams
import io.github.drumber.kitsune.data.model.media.BaseMedia
import io.github.drumber.kitsune.data.paging.RequestType
import io.github.drumber.kitsune.data.repository.AnimeRepository
import io.github.drumber.kitsune.data.repository.MangaRepository
import io.github.drumber.kitsune.data.service.Filter
import io.github.drumber.kitsune.preference.KitsunePref
import io.github.drumber.kitsune.ui.base.MediaCollectionViewModel
import kotlinx.coroutines.flow.Flow

class SearchViewModel(
    private val animeRepository: AnimeRepository,
    private val mangaRepository: MangaRepository
) : MediaCollectionViewModel() {

    override fun getStoredMediaSelector(): MediaSelector {
        return KitsunePref.searchParams.toMediaSelector(
            includeSortFilter = true,
            includeSearchQuery = false
        )
    }

    override fun getData(mediaSelector: MediaSelector): Flow<PagingData<BaseMedia>> {
        val filter = mediaSelector.filter
        return when (mediaSelector.mediaType) {
            MediaType.Anime -> animeRepository.animeCollection(
                Kitsu.DEFAULT_PAGE_SIZE,
                filter
            ) as Flow<PagingData<BaseMedia>>
            MediaType.Manga -> mangaRepository.mangaCollection(
                Kitsu.DEFAULT_PAGE_SIZE,
                filter
            ) as Flow<PagingData<BaseMedia>>
        }
    }

    private fun updateMediaSelector(searchParams: SearchParams) {
        val isSearching = isSearching.value == true
        val selector = searchParams.toMediaSelector(
            includeSortFilter = !isSearching,
            includeSearchQuery = isSearching
        )
        setMediaSelector(selector)
    }

    private fun SearchParams.toMediaSelector(
        includeSortFilter: Boolean,
        includeSearchQuery: Boolean
    ): MediaSelector {
        val params = this
        return MediaSelector(
            mediaType = params.mediaType,
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
        updateMediaSelector(searchParams)
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
        updateMediaSelector(searchParams)
    }

    fun resetSearch() {
        searchQuery = null
        _isSearching.value = false
        updateMediaSelector(searchParams)
    }

    fun restoreDefaultFilter() {
        val defaultSearchParams = Defaults.DEFAULT_SEARCH_PARAMS
        KitsunePref.searchParams = defaultSearchParams
        KitsunePref.searchCategories = emptyList()
        updateMediaSelector(defaultSearchParams)
    }

}