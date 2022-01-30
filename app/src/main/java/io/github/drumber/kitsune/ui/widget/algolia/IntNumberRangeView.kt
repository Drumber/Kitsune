package io.github.drumber.kitsune.ui.widget.algolia

import com.algolia.instantsearch.core.Callback
import com.algolia.instantsearch.core.number.range.NumberRangeView
import com.algolia.instantsearch.core.number.range.Range
import com.algolia.instantsearch.core.searcher.Debouncer
import com.google.android.material.slider.RangeSlider
import kotlinx.coroutines.CoroutineScope

class IntNumberRangeView(
    private val slider: RangeSlider,
    coroutineScope: CoroutineScope,
    debounceMillis: Long = 300L
) : NumberRangeView<Int> {

    override var onRangeChanged: Callback<Range<Int>>? = null

    private var range: Range<Int>? = null
    private var bounds: Range<Int>? = null

    private val debouncer = Debouncer(debounceMillis)

    init {
        slider.stepSize = 1f
        slider.addOnChangeListener { slider, _, fromUser ->
            if (!fromUser) return@addOnChangeListener
            debouncer.debounce(coroutineScope) {
                val valueMin = slider.values[0].toInt()
                val valueMax = slider.values[1].toInt()
                if (range?.min != valueMin || range?.max != valueMax) {
                    onRangeChanged?.invoke(Range(valueMin..valueMax))
                }
            }
        }
    }

    override fun setRange(range: Range<Int>?) {
        if (range == null) {
            bounds?.let {
                slider.setValues(it.min.toFloat(), it.max.toFloat())
            }
        } else if (this.range != range) {
            slider.setValues(range.min.toFloat(), range.max.toFloat())
        }
        this.range = range
    }

    override fun setBounds(bounds: Range<Int>?) {
        bounds?.let {
            this.bounds = it
            slider.valueFrom = it.min.toFloat()
            slider.valueTo = it.max.toFloat()
            slider.setValues(
                range?.min?.toFloat() ?: it.min.toFloat(),
                range?.max?.toFloat() ?: it.max.toFloat()
            )
        }
    }

}