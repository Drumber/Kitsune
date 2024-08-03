package io.github.drumber.kitsune.ui.component

import androidx.paging.LoadState
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView

class LoadStateSpanSizeLookup<T : Any, VH : RecyclerView.ViewHolder>(
    private val adapter: PagingDataAdapter<T, VH>,
    private val gridLayoutManager: GridLayoutManager
) : GridLayoutManager.SpanSizeLookup() {

    private var append: LoadState? = null
    private var prepend: LoadState? = null

    init {
        adapter.addLoadStateListener {
            append = it.append
            prepend = it.prepend
        }
    }

    override fun getSpanSize(position: Int): Int {
        if(position == 0 && prepend !is LoadState.NotLoading) {
            return gridLayoutManager.spanCount
        }
        val totalCount = adapter.itemCount.plus(if(prepend !is LoadState.NotLoading) 1 else 0)
        if(position >= totalCount && append !is LoadState.NotLoading) {
            return gridLayoutManager.spanCount
        }
        return 1
    }

}