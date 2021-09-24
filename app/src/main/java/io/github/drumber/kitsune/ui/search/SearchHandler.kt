package io.github.drumber.kitsune.ui.search

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.algolia.instantsearch.core.connection.ConnectionHandler
import com.algolia.instantsearch.helper.searchbox.SearchBoxConnector
import com.algolia.instantsearch.helper.searcher.SearcherSingleIndex
import com.algolia.search.client.ClientSearch
import com.algolia.search.client.Index
import com.algolia.search.model.APIKey
import com.algolia.search.model.ApplicationID
import com.algolia.search.model.IndexName
import com.algolia.search.model.response.ResponseSearch
import io.github.drumber.kitsune.constants.Kitsu
import io.github.drumber.kitsune.data.repository.AlgoliaKeyRepository
import io.github.drumber.kitsune.util.logE
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SearchHandler(
    private val algoliaKeyRepository: AlgoliaKeyRepository,
    private val coroutineScope: CoroutineScope
) {

    private val _isInitialized = MutableLiveData(false)
    val isInitialized: LiveData<Boolean>
        get() = _isInitialized

    private lateinit var searchClient: ClientSearch
    private lateinit var searchIndex: Index
    lateinit var searcher: SearcherSingleIndex
    lateinit var searchBox: SearchBoxConnector<ResponseSearch>
    private lateinit var connection: ConnectionHandler

    private fun initSearchClient() {
        coroutineScope.launch(Dispatchers.IO) {
            val algoliaKeys = try {
                algoliaKeyRepository.getAlgoliaKeys()
            } catch (e: Exception) {
                logE("Failed to obtain algolia keys.", e)
                null
            }
            val apiKey = algoliaKeys?.media?.key
            val index = algoliaKeys?.media?.index

            if(apiKey != null && index != null) {
                searchClient = ClientSearch(
                    applicationID = ApplicationID(Kitsu.ALGOLIA_APP_ID),
                    apiKey = APIKey(apiKey)
                )
                val indexName = IndexName(index)
                searchIndex = searchClient.initIndex(indexName)
                searcher = SearcherSingleIndex(searchIndex)

                searchBox = SearchBoxConnector(searcher)
                connection = ConnectionHandler()
                connection += searchBox

                _isInitialized.postValue(true)
            }
        }
    }

    init {
        initSearchClient()
    }

    fun cancel() {
        if(this::searcher.isInitialized) return
        searcher.cancel()
        connection.clear()
    }

}