package io.github.drumber.kitsune.ui.component.algolia.range

import com.algolia.instantsearch.core.Callback
import com.algolia.instantsearch.core.connection.AbstractConnection
import com.algolia.instantsearch.core.connection.Connection
import com.algolia.instantsearch.core.number.range.NumberRangeViewModel
import com.algolia.instantsearch.core.number.range.Range

data class CustomNumberRangeConnectionView<T>(
    private val viewModel: NumberRangeViewModel<T>,
    private val view: CustomNumberRangeView<T>
) : AbstractConnection() where T : Number, T : Comparable<T> {

    private val updateBounds: Callback<Range<T>?> = { bounds ->
        view.setBounds(bounds)
    }
    private val updateRange: Callback<Range<T>?> = { range ->
        view.setRange(range)
    }

    override fun connect() {
        super.connect()
        viewModel.bounds.subscribePast(updateBounds)
        viewModel.range.subscribePast(updateRange)
        view.onRangeChanged = (viewModel.eventRange::send)
    }

    override fun disconnect() {
        super.disconnect()
        viewModel.bounds.unsubscribe(updateBounds)
        viewModel.range.unsubscribe(updateRange)
        view.onRangeChanged = null
    }
}

fun <T> CustomFilterRangeConnector<T>.connectView(
    view: CustomNumberRangeView<T>,
): Connection where T : Number, T : Comparable<T> {
    return CustomNumberRangeConnectionView(viewModel, view)
}
