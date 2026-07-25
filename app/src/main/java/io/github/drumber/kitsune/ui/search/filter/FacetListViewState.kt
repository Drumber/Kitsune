package io.github.drumber.kitsune.ui.search.filter

import com.algolia.instantsearch.core.selectable.list.SelectableListView
import com.algolia.search.model.search.Facet
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/** Bridges an Algolia [SelectableListView] for facets to a Compose-observable [StateFlow]. */
class FacetListViewState : SelectableListView<Facet> {

    private val _items = MutableStateFlow<List<Pair<Facet, Boolean>>>(emptyList())
    val items: StateFlow<List<Pair<Facet, Boolean>>> = _items.asStateFlow()

    override var onSelection: ((Facet) -> Unit)? = null

    override fun setItems(items: List<Pair<Facet, Boolean>>) {
        _items.value = items
    }

    fun select(facet: Facet) {
        onSelection?.invoke(facet)
    }
}
