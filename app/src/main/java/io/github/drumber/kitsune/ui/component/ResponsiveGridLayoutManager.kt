package io.github.drumber.kitsune.ui.component

import android.content.Context
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlin.math.max

class ResponsiveGridLayoutManager(
    context: Context,
    private val columnWidth: Int,
    private val minColumns: Int = 1,
    @RecyclerView.Orientation orientation: Int = RecyclerView.VERTICAL,
    reverseLayout: Boolean = false
) : GridLayoutManager(context, 1, orientation, reverseLayout) {

    override fun onLayoutChildren(recycler: RecyclerView.Recycler?, state: RecyclerView.State?) {
        val availableWidth = width - paddingRight - paddingLeft
        spanCount = max(minColumns, availableWidth / columnWidth)
        super.onLayoutChildren(recycler, state)
    }
}