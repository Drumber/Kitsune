package io.github.drumber.kitsune.ui.widget.algolia.range

import com.algolia.instantsearch.core.Callback
import com.algolia.instantsearch.core.number.range.Range

interface CustomNumberRangeView<T> where T : Number, T : Comparable<T> {

    var onRangeChanged: Callback<Range<T>?>?

    fun setRange(range: Range<T>?)

    fun setBounds(bounds: Range<T>?)
}