package io.github.drumber.kitsune.ui.search.filter

import com.algolia.instantsearch.core.Callback
import com.algolia.instantsearch.core.number.range.Range
import io.github.drumber.kitsune.ui.component.algolia.range.CustomNumberRangeView
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/** Bridges Algolia [CustomNumberRangeView] for Int ranges to a Compose-observable [StateFlow]. */
class NumberRangeViewState : CustomNumberRangeView<Int> {

    private val _range = MutableStateFlow<Range<Int>?>(null)
    private val _bounds = MutableStateFlow<Range<Int>?>(null)
    val range: StateFlow<Range<Int>?> = _range.asStateFlow()
    val bounds: StateFlow<Range<Int>?> = _bounds.asStateFlow()

    override var onRangeChanged: Callback<Range<Int>?>? = null

    override fun setRange(range: Range<Int>?) {
        _range.value = range
    }

    override fun setBounds(bounds: Range<Int>?) {
        _bounds.value = bounds
    }

    fun changeRange(range: Range<Int>?) {
        onRangeChanged?.invoke(range)
    }
}
