package io.github.drumber.kitsune.ui.search

import androidx.lifecycle.*
import androidx.paging.cachedIn
import com.algolia.instantsearch.core.connection.ConnectionHandler
import com.algolia.instantsearch.core.connection.ConnectionImpl
import com.algolia.instantsearch.core.selectable.list.SelectionMode
import com.algolia.instantsearch.helper.filter.facet.FacetListConnector
import com.algolia.instantsearch.helper.filter.facet.FacetListPresenterImpl
import com.algolia.instantsearch.helper.filter.facet.FacetSortCriterion
import com.algolia.instantsearch.helper.filter.range.FilterRangeConnector
import com.algolia.instantsearch.helper.filter.state.FilterState
import com.algolia.instantsearch.helper.filter.state.Filters
import com.algolia.instantsearch.helper.filter.state.groupOr
import com.algolia.instantsearch.helper.searcher.SearcherSingleIndex
import com.algolia.instantsearch.helper.searcher.connectFilterState
import com.algolia.search.dsl.*
import com.algolia.search.model.Attribute
import com.algolia.search.model.filter.Filter
import com.algolia.search.model.response.ResponseSearch
import com.algolia.search.model.search.Query
import io.github.drumber.kitsune.constants.Kitsu
import io.github.drumber.kitsune.data.model.FilterCollection
import io.github.drumber.kitsune.data.model.auth.SearchType
import io.github.drumber.kitsune.data.model.media.MediaSearchResult
import io.github.drumber.kitsune.data.model.toCombinedMap
import io.github.drumber.kitsune.data.model.toFilterCollection
import io.github.drumber.kitsune.data.repository.AlgoliaKeyRepository
import io.github.drumber.kitsune.data.repository.SearchRepository
import io.github.drumber.kitsune.exception.SearchProviderUnavailableException
import io.github.drumber.kitsune.preference.KitsunePref
import io.github.drumber.kitsune.util.algolia.SearchBoxConnectorPaging
import io.github.drumber.kitsune.util.algolia.SeasonListPresenter
import io.github.drumber.kitsune.util.algolia.connectPaging
import io.github.drumber.kitsune.util.logE
import io.github.drumber.kitsune.util.logI
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromJsonElement
import java.util.*

class SearchViewModel(
    algoliaKeyRepository: AlgoliaKeyRepository
) : ViewModel() {

    private val searchProvider = SearchProvider(algoliaKeyRepository, defaultIndexSettings)

    private val searchSelector = MutableLiveData<Pair<SearchType, SearcherSingleIndex>>()

    private var filterState: FilterState? = null

    private var _filtersLiveData = MutableLiveData<Filters?>()
    val filtersLiveData get() = _filtersLiveData as LiveData<Filters?>

    private val _searchClientStatus = MutableLiveData(SearchClientStatus.NotInitialized)
    val searchClientStatus get() = _searchClientStatus as LiveData<SearchClientStatus>

    private val _searchBox = MutableLiveData<SearchBoxConnectorPaging<ResponseSearch>>()
    val searchBox get() = _searchBox as LiveData<SearchBoxConnectorPaging<ResponseSearch>>

    private val _filterFacets = MutableLiveData<FilterFacets>()
    val filterFacets get() = _filterFacets as LiveData<FilterFacets>

    private val connectionHandler = ConnectionHandler()

    private val json = Json { ignoreUnknownKeys = true }

    private val defaultIndexSettings
        get() = settings {
            queryLanguages {
                +English
                +Japanese
            }
            responseFields {
                +Hits
                +HitsPerPage
                +NbHits
                +NbPages
                +Offset
                +Page
            }
        }

    init {
        initializeSearchClient()
    }

    fun initializeSearchClient() {
        if (searchProvider.isInitialized) return
        val query = query {
            attributesToRetrieve {
                +"id"
                +"slug"
                +"kind"
                +"canonicalTitle"
                +"titles"
                +"posterImage"
                +"subtype"
            }
        }
        createSearchClient(SearchType.Media, query)
    }

    private fun createSearchClient(searchType: SearchType, query: Query) {
        viewModelScope.launch {
            filterState = null
            _searchClientStatus.postValue(SearchClientStatus.NotInitialized)
            try {
                searchProvider.createSearchClient(searchType, query) { searcher ->
                    connectionHandler.clear()
                    searchSelector.postValue(Pair(searchType, searcher))

                    val filterState = if (KitsunePref.rememberSearchFilters) {
                        val storedFilters = KitsunePref.searchFilters.toCombinedMap()
                        FilterState(storedFilters)
                    } else {
                        FilterState()
                    }
                    createFilterFacets(searcher, filterState)
                    connectionHandler += searcher.connectFilterState(filterState)
                    connectionHandler += filterState.connectPaging { SearchRepository.invalidate() }

                    _filtersLiveData.postValue(filterState.filters.value)
                    filterState.filters.subscribe {
                        _filtersLiveData.postValue(it)
                        // store search filters
                        KitsunePref.searchFilters = it.toFilterCollection()
                    }

                    createSearchBox(searcher)
                    this@SearchViewModel.filterState = filterState
                    _searchClientStatus.postValue(SearchClientStatus.Initialized)
                }
            } catch (e: SearchProviderUnavailableException) {
                logI("Search provider not available. Is the device offline?")
                _searchClientStatus.postValue(SearchClientStatus.NotAvailable)
            } catch (e: Exception) {
                logE("Could not create search client.", e)
                _searchClientStatus.postValue(SearchClientStatus.Error)
            }
        }
    }

    private fun createSearchBox(searcher: SearcherSingleIndex) {
        val searchBox = SearchBoxConnectorPaging(searcher) {
            SearchRepository.invalidate()
        }
        connectionHandler += searchBox
        _searchBox.postValue(searchBox)
    }

    val searchResultSource = searchSelector.asFlow().flatMapLatest { selector ->
        val (searchType, searcher) = selector
        SearchRepository.search(Kitsu.DEFAULT_PAGE_SIZE, searcher) { hit ->
            when (searchType) {
                SearchType.Media -> json.decodeFromJsonElement<MediaSearchResult>(hit.json)
                else -> throw IllegalStateException("Search type '$searchType' is not supported.")
            }
        }
    }.cachedIn(viewModelScope)


    private fun createFilterFacets(searcher: SearcherSingleIndex, filterState: FilterState) {
        val filterFacets = FilterFacets(searcher, filterState)
        applyCategoryFilters(filterState)
        _filterFacets.postValue(filterFacets)
    }

    fun clearSearchFilter() {
        filterState?.notify {
            clear(*getGroups().keys.toTypedArray())
        }
        KitsunePref.searchFilters = FilterCollection()
        KitsunePref.searchCategories = emptyList()
        _filtersLiveData.postValue(null)
    }

    private fun applyCategoryFilters(filterState: FilterState) {
        filterState.notify {
            val categories = Attribute("categories")
            val filterFacets = KitsunePref.searchCategories.mapNotNull { wrapper ->
                wrapper.categoryName?.let { categoryName ->
                    Filter.Facet(categories, categoryName)
                }
            }

            val group = groupOr(categories)
            clear(group)
            if (filterFacets.isNotEmpty()) {
                add(group, *filterFacets.toTypedArray())
            }
        }
    }

    fun updateCategoryFilters() {
        filterState?.let { applyCategoryFilters(it) }
    }

    override fun onCleared() {
        super.onCleared()
        searchProvider.cancel()
        connectionHandler.clear()
    }

    inner class FilterFacets(
        searcher: SearcherSingleIndex,
        filterState: FilterState
    ) {

        val kindConnector = FacetListConnector(
            searcher = searcher,
            filterState = filterState,
            attribute = Attribute("kind"),
            selectionMode = SelectionMode.Multiple,
        ).bind()
        val kindPresenter = FacetListPresenterImpl(limit = 2)

        val yearConnector = FilterRangeConnector(
            filterState = filterState,
            attribute = Attribute("year"),
            range = minYear..maxYear,
            bounds = minYear..maxYear
        ).bind()

        val avgRatingConnector = FilterRangeConnector(
            filterState = filterState,
            attribute = Attribute("averageRating"),
            range = 5..100,
            bounds = 5..100
        ).bind()

        val seasonConnector = FacetListConnector(
            searcher = searcher,
            filterState = filterState,
            attribute = Attribute("season"),
            selectionMode = SelectionMode.Multiple,
        ).bind()
        val seasonPresenter = SeasonListPresenter()

        val subtypeConnector = FacetListConnector(
            searcher = searcher,
            filterState = filterState,
            attribute = Attribute("subtype"),
            selectionMode = SelectionMode.Multiple,
        ).bind()
        val subtypePresenter = FacetListPresenterImpl(limit = 100, sortBy = defaultFacetSortBy)

        val streamersConnector = FacetListConnector(
            searcher = searcher,
            filterState = filterState,
            attribute = Attribute("streamers"),
            selectionMode = SelectionMode.Multiple,
        ).bind()
        val streamersPresenter = FacetListPresenterImpl(limit = 100, sortBy = defaultFacetSortBy)

        val ageRatingConnector = FacetListConnector(
            searcher = searcher,
            filterState = filterState,
            attribute = Attribute("ageRating"),
            selectionMode = SelectionMode.Multiple,
        ).bind()
        val ageRatingPresenter = FacetListPresenterImpl(limit = 4, sortBy = defaultFacetSortBy)

        private fun <T : ConnectionImpl> T.bind() = apply { connectionHandler += this }
    }

    companion object {
        private val defaultFacetSortBy
            get() = listOf(
                FacetSortCriterion.IsRefined,
                FacetSortCriterion.CountDescending
            )

        val maxYear get() = Calendar.getInstance().get(Calendar.YEAR) + 2
        const val minYear = 1862
    }

    enum class SearchClientStatus {
        NotInitialized,
        Initialized,
        NotAvailable,
        Error
    }

}
