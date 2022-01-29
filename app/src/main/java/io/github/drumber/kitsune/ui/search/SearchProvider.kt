package io.github.drumber.kitsune.ui.search

import com.algolia.instantsearch.helper.searcher.SearcherSingleIndex
import com.algolia.search.client.ClientSearch
import com.algolia.search.model.APIKey
import com.algolia.search.model.ApplicationID
import com.algolia.search.model.IndexName
import com.algolia.search.model.search.Query
import com.algolia.search.model.settings.Settings
import io.github.drumber.kitsune.BuildConfig
import io.github.drumber.kitsune.constants.Kitsu
import io.github.drumber.kitsune.data.model.auth.SearchType
import io.github.drumber.kitsune.data.repository.AlgoliaKeyRepository
import io.github.drumber.kitsune.exception.InvalidDataException
import io.github.drumber.kitsune.exception.SearchProviderUnavailableException
import io.github.drumber.kitsune.util.logE
import io.ktor.client.features.logging.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.withContext

class SearchProvider(
    private val algoliaKeyRepository: AlgoliaKeyRepository,
    private val defaultIndexSettings: Settings = Settings()
) {

    private var clientSearch: ClientSearch? = null
    private var searcherIndex: SearcherSingleIndex? = null

    suspend fun createSearchClient(searchType: SearchType, query: Query, createdListener: SearcherCreatedListener) {
        searcherIndex?.cancel() // cancel any previous created searcher

        val algoliaKeys = getAlgoliaKeysAsync().await()
            ?: throw SearchProviderUnavailableException()
        val algoliaKey = searchType.getAlgoliaKey(algoliaKeys)
        val apiKey = algoliaKey?.key ?: throw InvalidDataException("Algolia API Key is null.")
        val apiIndex = algoliaKey.index ?: throw InvalidDataException("Algolia index is null.")

        val logLevel = if (BuildConfig.DEBUG) LogLevel.ALL else LogLevel.NONE
        val client = ClientSearch(ApplicationID(Kitsu.ALGOLIA_APP_ID), APIKey(apiKey), logLevel)
        val index = client.initIndex(IndexName(apiIndex))
        //index.setSettings(defaultIndexSettings)

        val searcher = SearcherSingleIndex(index, query)
        clientSearch = client
        searcherIndex = searcher

        createdListener.onSearcherCreated(searcher)
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

fun interface SearcherCreatedListener {
    fun onSearcherCreated(searcher: SearcherSingleIndex)
}
