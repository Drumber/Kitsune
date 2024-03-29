package io.github.drumber.kitsune.domain.manager

import com.algolia.instantsearch.searcher.hits.HitsSearcher
import com.algolia.instantsearch.searcher.hits.SearchForQuery
import com.algolia.search.client.ClientSearch
import com.algolia.search.logging.LogLevel
import com.algolia.search.model.APIKey
import com.algolia.search.model.ApplicationID
import com.algolia.search.model.IndexName
import com.algolia.search.model.search.Query
import io.github.drumber.kitsune.BuildConfig
import io.github.drumber.kitsune.constants.Kitsu
import io.github.drumber.kitsune.domain.model.infrastructure.algolia.SearchType
import io.github.drumber.kitsune.domain.repository.AlgoliaKeyRepository
import io.github.drumber.kitsune.exception.InvalidDataException
import io.github.drumber.kitsune.exception.SearchProviderUnavailableException
import io.github.drumber.kitsune.util.logE
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.withContext

class SearchProvider(
    private val algoliaKeyRepository: AlgoliaKeyRepository
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

        val logLevel = if (BuildConfig.DEBUG) LogLevel.All else LogLevel.None
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
                algoliaKeyRepository.getAlgoliaKeys()
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
