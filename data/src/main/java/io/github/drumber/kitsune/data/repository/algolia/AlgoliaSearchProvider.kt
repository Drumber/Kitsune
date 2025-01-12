package io.github.drumber.kitsune.data.repository.algolia

import com.algolia.instantsearch.searcher.hits.HitsSearcher
import com.algolia.instantsearch.searcher.hits.SearchForQuery
import com.algolia.search.client.ClientSearch
import com.algolia.search.logging.LogLevel
import com.algolia.search.model.APIKey
import com.algolia.search.model.ApplicationID
import com.algolia.search.model.IndexName
import com.algolia.search.model.search.Query
import io.github.drumber.kitsune.data.exception.InvalidDataException
import io.github.drumber.kitsune.data.exception.SearchProviderUnavailableException
import io.github.drumber.kitsune.data.model.algolia.SearchType
import io.github.drumber.kitsune.shared.constants.Kitsu
import io.github.drumber.kitsune.shared.logE
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.withContext

class AlgoliaSearchProvider(
    private val algoliaKeyRepository: AlgoliaKeyRepository,
    private val logLevel: LogLevel = LogLevel.Info
) {

    private var clientSearch: ClientSearch? = null
    private var searcherIndex: HitsSearcher? = null

    var isInitialized = false
        private set

    suspend fun createSearchClient(
        searchType: SearchType,
        query: Query,
        triggerSearchFor: SearchForQuery = SearchForQuery.All,
        createdListener: suspend (HitsSearcher) -> Unit
    ) {
        searcherIndex?.cancel() // cancel any previous created searcher
        isInitialized = false

        val algoliaKeys = getAlgoliaKeysAsync().await()
            ?: throw SearchProviderUnavailableException()
        val algoliaKey = searchType.getAlgoliaKey(algoliaKeys)
        val apiKey = algoliaKey?.key ?: throw InvalidDataException("Algolia API Key is null.")
        val apiIndex = algoliaKey.index ?: throw InvalidDataException("Algolia index is null.")

        val client = ClientSearch(ApplicationID(Kitsu.ALGOLIA_APP_ID), APIKey(apiKey), logLevel)

        val searcher = HitsSearcher(
            client, IndexName(apiIndex), query,
            triggerSearchFor = triggerSearchFor
        )
        clientSearch = client
        searcherIndex = searcher

        isInitialized = true
        withContext(Dispatchers.Main) {
            createdListener(searcher)
        }
    }

    private suspend fun getAlgoliaKeysAsync() = withContext(Dispatchers.IO) {
        async {
            try {
                algoliaKeyRepository.getAllAlgoliaKeys()
            } catch (e: Exception) {
                logE("Failed to obtain algolia search keys.", e)
                null
            }
        }
    }

    fun cancel() {
        searcherIndex?.cancel()
    }

}
