package io.github.drumber.kitsune.ui.search

import androidx.lifecycle.*
import androidx.paging.cachedIn
import com.algolia.instantsearch.core.connection.ConnectionHandler
import com.algolia.instantsearch.helper.searcher.SearcherSingleIndex
import com.algolia.search.dsl.*
import com.algolia.search.model.search.Query
import io.github.drumber.kitsune.constants.Kitsu
import io.github.drumber.kitsune.data.model.auth.SearchType
import io.github.drumber.kitsune.data.model.media.MediaSearchResult
import io.github.drumber.kitsune.data.repository.AlgoliaKeyRepository
import io.github.drumber.kitsune.data.repository.SearchRepository
import io.github.drumber.kitsune.util.algolia.SearchBoxConnectorPaging
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromJsonElement

class SearchViewModel(
    algoliaKeyRepository: AlgoliaKeyRepository
) : ViewModel() {

    private val searchProvider = SearchProvider(algoliaKeyRepository, defaultIndexSettings)

    private val searchSelector = MutableLiveData<Pair<SearchType, SearcherSingleIndex>>()

    private val _searchBox = MutableLiveData<SearchBoxConnectorPaging<*>>()
    val searchBox get() = _searchBox as LiveData<SearchBoxConnectorPaging<*>>

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

    fun createSearchClient(searchType: SearchType, query: Query) {
        viewModelScope.launch {
            searchProvider.createSearchClient(searchType, query) { searcher ->
                searchSelector.postValue(Pair(searchType, searcher))
                createSearchBox(searcher)
            }
        }
    }

    private fun createSearchBox(searcher: SearcherSingleIndex) {
        val searchBox = SearchBoxConnectorPaging(searcher) {
            SearchRepository.invalidate()
        }
        connectionHandler.clear()
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

    override fun onCleared() {
        super.onCleared()
        searchProvider.cancel()
        connectionHandler.clear()
    }

}
