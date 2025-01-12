package io.github.drumber.kitsune.ui.search

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asFlow
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingConfig
import androidx.paging.cachedIn
import com.algolia.instantsearch.android.paging3.Paginator
import com.algolia.instantsearch.android.paging3.filterstate.connectPaginator
import com.algolia.instantsearch.android.paging3.flow
import com.algolia.instantsearch.android.paging3.searchbox.connectPaginator
import com.algolia.instantsearch.core.connection.AbstractConnection
import com.algolia.instantsearch.core.connection.ConnectionHandler
import com.algolia.instantsearch.core.selectable.list.SelectionMode
import com.algolia.instantsearch.filter.facet.DefaultFacetListPresenter
import com.algolia.instantsearch.filter.facet.FacetListConnector
import com.algolia.instantsearch.filter.facet.FacetSortCriterion
import com.algolia.instantsearch.filter.state.FilterState
import com.algolia.instantsearch.filter.state.Filters
import com.algolia.instantsearch.filter.state.groupOr
import com.algolia.instantsearch.searchbox.SearchBoxConnector
import com.algolia.instantsearch.searcher.connectFilterState
import com.algolia.instantsearch.searcher.hits.HitsSearcher
import com.algolia.search.dsl.attributesToRetrieve
import com.algolia.search.dsl.query
import com.algolia.search.model.Attribute
import com.algolia.search.model.filter.Filter
import com.algolia.search.model.response.ResponseSearch
import com.algolia.search.model.search.Query
import io.github.drumber.kitsune.data.exception.SearchProviderUnavailableException
import io.github.drumber.kitsune.data.mapper.AlgoliaMapper.toMedia
import io.github.drumber.kitsune.data.model.algolia.SearchType
import io.github.drumber.kitsune.data.model.media.Media
import io.github.drumber.kitsune.data.presentation.FilterCollection
import io.github.drumber.kitsune.data.presentation.toCombinedMap
import io.github.drumber.kitsune.data.presentation.toFilterCollection
import io.github.drumber.kitsune.data.repository.algolia.AlgoliaKeyRepository
import io.github.drumber.kitsune.data.repository.algolia.AlgoliaSearchProvider
import io.github.drumber.kitsune.data.source.algolia.AlgoliaMediaSearchResult
import io.github.drumber.kitsune.preference.KitsunePref
import io.github.drumber.kitsune.shared.constants.Kitsu
import io.github.drumber.kitsune.shared.constants.Repository
import io.github.drumber.kitsune.shared.logE
import io.github.drumber.kitsune.shared.logI
import io.github.drumber.kitsune.ui.component.algolia.SeasonListPresenter
import io.github.drumber.kitsune.ui.component.algolia.range.CustomFilterRangeConnector
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromJsonElement
import java.util.Calendar

class SearchViewModel(
    algoliaKeyRepository: AlgoliaKeyRepository
) : ViewModel() {

    private val searchProvider = AlgoliaSearchProvider(algoliaKeyRepository)

    private val searchSelector = MutableLiveData<Pair<SearchType, HitsSearcher>>()

    private var filterState: FilterState? = null

    private val searchPaginator = MutableLiveData<Paginator<Media>>()

    private val _filtersLiveData = MutableLiveData<Filters?>()
    val filtersLiveData get() = _filtersLiveData as LiveData<Filters?>

    private val _searchClientStatus = MutableLiveData(SearchClientStatus.NotInitialized)
    val searchClientStatus get() = _searchClientStatus as LiveData<SearchClientStatus>

    private val _searchBox = MutableLiveData<SearchBoxConnector<ResponseSearch>>()
    val searchBox get() = _searchBox as LiveData<SearchBoxConnector<ResponseSearch>>

    private val _filterFacets = MutableLiveData<FilterFacets>()
    val filterFacets get() = _filterFacets as LiveData<FilterFacets>

    private val connectionHandler = ConnectionHandler()

    private val json = Json { ignoreUnknownKeys = true }

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
                    searchPaginator.value?.invalidate()
                    connectionHandler.clear()
                    searchSelector.postValue(Pair(searchType, searcher))

                    val paginator = Paginator(
                        searcher = searcher,
                        pagingConfig = PagingConfig(
                            pageSize = Kitsu.DEFAULT_PAGE_SIZE,
                            maxSize = Repository.MAX_CACHED_ITEMS
                        ),
                        transformer = { hit ->
                            when (searchType) {
                                SearchType.Media -> json.decodeFromJsonElement<AlgoliaMediaSearchResult>(hit.json).toMedia()
                                else -> throw IllegalStateException("Search type '$searchType' is not supported.")
                            }
                        }
                    )
                    searchPaginator.postValue(paginator)

                    val filterState = if (KitsunePref.rememberSearchFilters) {
                        val storedFilters = KitsunePref.searchFilters.toCombinedMap()
                        FilterState(storedFilters)
                    } else {
                        FilterState()
                    }
                    createFilterFacets(searcher, filterState)
                    connectionHandler += searcher.connectFilterState(filterState)
                    connectionHandler += filterState.connectPaginator(paginator)

                    _filtersLiveData.postValue(filterState.filters.value)
                    filterState.filters.subscribe {
                        _filtersLiveData.postValue(it)
                        // store search filters
                        KitsunePref.searchFilters = it.toFilterCollection()
                    }

                    createSearchBox(searcher, paginator)
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

    private fun createSearchBox(searcher: HitsSearcher, paginator: Paginator<Media>) {
        val searchBox = SearchBoxConnector(searcher)
        connectionHandler += searchBox
        connectionHandler += searchBox.connectPaginator(paginator)
        _searchBox.postValue(searchBox)
    }

    val searchResultSource = searchPaginator.asFlow().flatMapLatest { paginator ->
        paginator.flow
    }.cachedIn(viewModelScope)


    private fun createFilterFacets(searcher: HitsSearcher, filterState: FilterState) {
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
        searcher: HitsSearcher,
        filterState: FilterState
    ) {

        val kindConnector = FacetListConnector(
            searcher = searcher,
            filterState = filterState,
            attribute = Attribute("kind"),
            selectionMode = SelectionMode.Multiple,
        ).bind()
        val kindPresenter = DefaultFacetListPresenter(limit = 2)

        val yearConnector = CustomFilterRangeConnector(
            filterState = filterState,
            attribute = Attribute("year"),
            range = minYear..maxYear,
            bounds = minYear..maxYear
        ).bind()

        val avgRatingConnector = CustomFilterRangeConnector(
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
        val subtypePresenter = DefaultFacetListPresenter(limit = 100, sortBy = defaultFacetSortBy)

        val streamersConnector = FacetListConnector(
            searcher = searcher,
            filterState = filterState,
            attribute = Attribute("streamers"),
            selectionMode = SelectionMode.Multiple,
        ).bind()
        val streamersPresenter = DefaultFacetListPresenter(limit = 100, sortBy = defaultFacetSortBy)

        val ageRatingConnector = FacetListConnector(
            searcher = searcher,
            filterState = filterState,
            attribute = Attribute("ageRating"),
            selectionMode = SelectionMode.Multiple,
        ).bind()
        val ageRatingPresenter = DefaultFacetListPresenter(limit = 4, sortBy = defaultFacetSortBy)

        private fun <T : AbstractConnection> T.bind() = apply { connectionHandler += this }
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
