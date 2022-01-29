package io.github.drumber.kitsune.util.algolia

import com.algolia.instantsearch.core.connection.Connection
import com.algolia.instantsearch.core.connection.ConnectionImpl
import com.algolia.instantsearch.core.searchbox.SearchBoxView
import com.algolia.instantsearch.core.searchbox.SearchBoxViewModel
import com.algolia.instantsearch.core.searchbox.connectView
import com.algolia.instantsearch.core.searcher.Debouncer
import com.algolia.instantsearch.core.searcher.Searcher
import com.algolia.instantsearch.core.searcher.debounceSearchInMillis
import com.algolia.instantsearch.helper.searchbox.SearchMode

data class SearchBoxConnectorPaging<R>(
    val searcher: Searcher<R>,
    val viewModel: SearchBoxViewModel = SearchBoxViewModel(),
    val searchMode: SearchMode = SearchMode.AsYouType,
    val debouncer: Debouncer = Debouncer(debounceSearchInMillis),
    val invalidateCallback: () -> Unit
) : ConnectionImpl() {

    private val connectionSearcher = viewModel.connectSearcher(searcher, invalidateCallback, searchMode, debouncer)

    override fun connect() {
        super.connect()
        connectionSearcher.connect()
    }

    override fun disconnect() {
        super.disconnect()
        connectionSearcher.disconnect()
    }
}

fun <R> SearchBoxViewModel.connectSearcher(
    searcher: Searcher<R>,
    invalidateCallback: () -> Unit,
    searchAsYouType: SearchMode = SearchMode.AsYouType,
    debouncer: Debouncer = Debouncer(debounceSearchInMillis),
): Connection {
    return SearchBoxConnectionSearcherPaging(this, searcher, invalidateCallback, searchAsYouType, debouncer)
}

fun <R> SearchBoxConnectorPaging<R>.connectView(
    view: SearchBoxView,
): Connection {
    return viewModel.connectView(view)
}
