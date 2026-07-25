package io.github.drumber.kitsune.ui.search

import com.algolia.instantsearch.core.searchbox.SearchBoxView
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/** Bridges Algolia [SearchBoxView] to a Compose-observable [StateFlow] of the current query text. */
class SearchBoxViewCompose : SearchBoxView {

    private val _query = MutableStateFlow("")
    val queryFlow: StateFlow<String> = _query.asStateFlow()

    override var onQueryChanged: ((String?) -> Unit)? = null
    override var onQuerySubmitted: ((String?) -> Unit)? = null

    override fun setText(text: String?, submitQuery: Boolean) {
        _query.value = text.orEmpty()
    }

    fun notifyQueryChanged(text: String) {
        _query.value = text
        onQueryChanged?.invoke(text)
    }

    fun notifyQuerySubmitted(text: String) {
        _query.value = text
        onQuerySubmitted?.invoke(text)
    }
}
