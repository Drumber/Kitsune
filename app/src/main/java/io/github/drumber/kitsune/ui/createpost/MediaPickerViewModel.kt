package io.github.drumber.kitsune.ui.createpost

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asFlow
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingConfig
import androidx.paging.cachedIn
import com.algolia.instantsearch.android.paging3.Paginator
import com.algolia.instantsearch.android.paging3.flow
import com.algolia.instantsearch.android.paging3.searchbox.connectPaginator
import com.algolia.instantsearch.core.connection.ConnectionHandler
import com.algolia.instantsearch.searchbox.SearchBoxConnector
import com.algolia.search.dsl.attributesToRetrieve
import com.algolia.search.dsl.query
import com.algolia.search.model.response.ResponseSearch
import io.github.drumber.kitsune.config.Kitsu
import io.github.drumber.kitsune.config.Repository
import io.github.drumber.kitsune.data.common.exception.SearchProviderUnavailableException
import io.github.drumber.kitsune.data.mapper.AlgoliaMapper.toMedia
import io.github.drumber.kitsune.data.presentation.model.algolia.SearchType
import io.github.drumber.kitsune.data.presentation.model.media.Media
import io.github.drumber.kitsune.data.repository.AlgoliaKeyRepository
import io.github.drumber.kitsune.data.source.network.algolia.model.search.AlgoliaMediaSearchResult
import io.github.drumber.kitsune.domain.algolia.SearchProvider
import io.github.drumber.kitsune.util.logE
import io.github.drumber.kitsune.util.logI
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromJsonElement

/**
 * Lean Algolia media searcher for the post composer's media picker. Mirrors the media-only path of
 * [io.github.drumber.kitsune.ui.search.SearchViewModel] without category facets or filter state.
 */
class MediaPickerViewModel(
    algoliaKeyRepository: AlgoliaKeyRepository
) : ViewModel() {

    enum class Status { NotInitialized, Initialized, NotAvailable, Error }

    private val searchProvider = SearchProvider(algoliaKeyRepository)

    private val searchPaginator = MutableLiveData<Paginator<Media>>()

    private val _searchBox = MutableLiveData<SearchBoxConnector<ResponseSearch>>()
    val searchBox get() = _searchBox as LiveData<SearchBoxConnector<ResponseSearch>>

    private val _status = MutableLiveData(Status.NotInitialized)
    val status get() = _status as LiveData<Status>

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
        _status.postValue(Status.NotInitialized)
        viewModelScope.launch {
            try {
                searchProvider.createSearchClient(SearchType.Media, query) { searcher ->
                    searchPaginator.value?.invalidate()
                    connectionHandler.clear()

                    val paginator = Paginator(
                        searcher = searcher,
                        pagingConfig = PagingConfig(
                            pageSize = Kitsu.DEFAULT_PAGE_SIZE,
                            maxSize = Repository.MAX_CACHED_ITEMS
                        ),
                        transformer = { hit ->
                            json.decodeFromJsonElement<AlgoliaMediaSearchResult>(hit.json).toMedia()
                        }
                    )
                    searchPaginator.postValue(paginator)

                    val searchBox = SearchBoxConnector(searcher)
                    connectionHandler += searchBox
                    connectionHandler += searchBox.connectPaginator(paginator)
                    _searchBox.postValue(searchBox)

                    _status.postValue(Status.Initialized)
                }
            } catch (e: SearchProviderUnavailableException) {
                logI("Search provider not available. Is the device offline?", e)
                _status.postValue(Status.NotAvailable)
            } catch (e: Exception) {
                logE("Could not create media picker search client.", e)
                _status.postValue(Status.Error)
            }
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    val searchResultSource = searchPaginator.asFlow().flatMapLatest { paginator ->
        paginator.flow
    }.cachedIn(viewModelScope)

    override fun onCleared() {
        connectionHandler.clear()
        searchProvider.cancel()
    }
}
