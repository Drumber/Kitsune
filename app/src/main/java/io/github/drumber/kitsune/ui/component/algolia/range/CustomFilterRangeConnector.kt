package io.github.drumber.kitsune.ui.component.algolia.range

import com.algolia.instantsearch.core.connection.AbstractConnection
import com.algolia.instantsearch.core.connection.Connection
import com.algolia.instantsearch.core.number.range.Range
import com.algolia.instantsearch.filter.range.FilterRangeViewModel
import com.algolia.instantsearch.filter.state.FilterGroupID
import com.algolia.instantsearch.filter.state.FilterOperator
import com.algolia.instantsearch.filter.state.FilterState
import com.algolia.search.model.Attribute

data class CustomFilterRangeConnector<T>(
    val viewModel: FilterRangeViewModel<T>,
    val filterState: FilterState,
    val attribute: Attribute,
    val groupID: FilterGroupID = FilterGroupID(attribute, FilterOperator.And),
) : AbstractConnection() where T : Number, T : Comparable<T> {

    constructor(
        filterState: FilterState,
        attribute: Attribute,
        bounds: ClosedRange<T>? = null,
        range: ClosedRange<T>? = null,
    ) : this(
        FilterRangeViewModel(
            range = range?.let { Range(it) },
            bounds = bounds?.let { Range(it) }
        ),
        filterState, attribute
    )

    private val connectionFilterState = viewModel.connectFilterState(filterState, attribute, groupID)

    override fun connect() {
        super.connect()
        connectionFilterState.connect()
    }

    override fun disconnect() {
        super.disconnect()
        connectionFilterState.disconnect()
    }
}

fun <T> FilterRangeViewModel<T>.connectFilterState(
    filterState: FilterState,
    attribute: Attribute,
    groupID: FilterGroupID = FilterGroupID(attribute, FilterOperator.And),
): Connection where T : Number, T : Comparable<T> {
    return CustomFilterRangeConnectionFilterState(this, filterState, attribute, groupID)
}
