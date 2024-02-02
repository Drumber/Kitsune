package io.github.drumber.kitsune.ui.widget.algolia.range

import com.algolia.instantsearch.core.Callback
import com.algolia.instantsearch.core.connection.AbstractConnection
import com.algolia.instantsearch.core.number.range.Range
import com.algolia.instantsearch.filter.range.FilterRangeViewModel
import com.algolia.instantsearch.filter.state.FilterGroupID
import com.algolia.instantsearch.filter.state.FilterState
import com.algolia.instantsearch.filter.state.Filters
import com.algolia.instantsearch.filter.state.toFilterNumeric
import com.algolia.search.model.Attribute
import com.algolia.search.model.filter.Filter

data class CustomFilterRangeConnectionFilterState<T>(
    private val viewModel: FilterRangeViewModel<T>,
    private val filterState: FilterState,
    private val attribute: Attribute,
    private val groupID: FilterGroupID,
) : AbstractConnection() where T : Number, T : Comparable<T> {

    @Suppress("UNCHECKED_CAST")
    private val updateRange: Callback<Filters> = { filters ->
        val filter = filters.getNumericFilters(groupID)
            .filter { it.attribute == attribute }
            .map { it.value }
            .filterIsInstance<Filter.Numeric.Value.Range>()
            .firstOrNull()

        if (filter != null) {
            viewModel.range.value = Range(filter.lowerBound as T, filter.upperBound as T)
        } else {
            // set range value to null if the filter is not set
            viewModel.range.value = null
        }
    }

    private val updateFilterState: Callback<Range<T>?> = { range ->
        filterState.notify {
            viewModel.range.value?.let { remove(groupID, it.toFilterNumeric(attribute)) }
            if (range != null) add(groupID, range.toFilterNumeric(attribute))
        }
    }

    override fun connect() {
        super.connect()
        filterState.filters.subscribePast(updateRange)
        viewModel.eventRange.subscribe(updateFilterState)
    }

    override fun disconnect() {
        super.disconnect()
        filterState.filters.unsubscribe(updateRange)
        viewModel.eventRange.unsubscribe(updateFilterState)
    }
}
