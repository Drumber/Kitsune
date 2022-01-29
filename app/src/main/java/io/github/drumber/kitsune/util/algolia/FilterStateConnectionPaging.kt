package io.github.drumber.kitsune.util.algolia

import com.algolia.instantsearch.core.Callback
import com.algolia.instantsearch.core.connection.Connection
import com.algolia.instantsearch.core.connection.ConnectionImpl
import com.algolia.instantsearch.helper.filter.state.FilterState
import com.algolia.instantsearch.helper.filter.state.Filters

class FilterStateConnectionPaging(
    private val filterState: FilterState,
    private val invalidateCallback: () -> Unit
) : ConnectionImpl() {

    private val updateFilterState: Callback<Filters> = {
        invalidateCallback()
    }

    override fun connect() {
        super.connect()
        filterState.filters.subscribe(updateFilterState)
    }

    override fun disconnect() {
        super.disconnect()
        filterState.filters.unsubscribe(updateFilterState)
    }
}

fun FilterState.connectPaging(invalidateCallback: () -> Unit): Connection {
    return FilterStateConnectionPaging(this, invalidateCallback)
}
