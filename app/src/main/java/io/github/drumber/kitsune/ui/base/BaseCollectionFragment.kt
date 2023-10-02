package io.github.drumber.kitsune.ui.base

import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.annotation.LayoutRes
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.paging.CombinedLoadStates
import androidx.paging.LoadState
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.ConcatAdapter
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.navigation.NavigationBarView
import io.github.drumber.kitsune.R
import io.github.drumber.kitsune.databinding.LayoutResourceLoadingBinding
import io.github.drumber.kitsune.preference.KitsunePref
import io.github.drumber.kitsune.ui.adapter.paging.ResourceLoadStateAdapter
import io.github.drumber.kitsune.ui.widget.LoadStateSpanSizeLookup
import kotlin.math.floor
import kotlin.math.max

abstract class BaseCollectionFragment(@LayoutRes contentLayoutId: Int) :
    Fragment(contentLayoutId),
    View.OnClickListener,
    NavigationBarView.OnItemReselectedListener {

    abstract val recyclerView: RecyclerView

    abstract val resourceLoadingBinding: LayoutResourceLoadingBinding?

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initRecyclerView(recyclerView)
        resourceLoadingBinding?.btnRetry?.setOnClickListener(this)
    }

    protected open fun initRecyclerView(recyclerView: RecyclerView) {
        val gridLayout = GridLayoutManager(requireContext(), 2)
        recyclerView.layoutManager = gridLayout
        recyclerView.post {
            if (isAdded) {
                // calculate span count in relation to the recycler view width
                val width = recyclerView.width
                val cellWidth = resources.getDimension(KitsunePref.mediaItemSize.widthRes) +
                        2 * resources.getDimension(R.dimen.media_item_margin)
                val spanCount = floor(width / cellWidth).toInt()
                gridLayout.spanCount =
                    max(2, spanCount) // set new span count with minimum 2 columns
            }
        }
    }

    fun setRecyclerViewAdapter(adapter: RecyclerView.Adapter<*>) {
        recyclerView.adapter?.let { oldAdapter ->
            removeLoadStateListenerFromAdapter(oldAdapter)
        }

        recyclerView.adapter = if (adapter is PagingDataAdapter<*, *>) {
            (recyclerView.layoutManager as? GridLayoutManager)?.let { gridLayout ->
                // this will make sure to display header and footer with full width
                gridLayout.spanSizeLookup = LoadStateSpanSizeLookup(adapter, gridLayout.spanCount)
            }
            
            adapter.applyLoadStateListenerWithLoadStateHeaderAndFooter()
        } else {
            adapter
        }
    }

    fun removeLoadStateListenerFromAdapter(adapter: RecyclerView.Adapter<*>) {
        if (adapter is PagingDataAdapter<*, *>) {
            adapter.removeLoadStateListener(loadStateListener)
        } else if (adapter is ConcatAdapter) {
            adapter.adapters.forEach { removeLoadStateListenerFromAdapter(it) }
        }
    }

    fun PagingDataAdapter<*, *>.applyLoadStateListenerWithLoadStateHeaderAndFooter(): ConcatAdapter {
        this.addLoadStateListener(loadStateListener)

        return this.withLoadStateHeaderAndFooter(
            header = ResourceLoadStateAdapter(this),
            footer = ResourceLoadStateAdapter(this)
        )
    }

    /** Triggered when clicking on retry button. */
    override fun onClick(retryButton: View?) {
        (recyclerView.adapter as? PagingDataAdapter<*, *>)?.retry()
        (recyclerView.adapter as? ConcatAdapter)?.adapters
            ?.filterIsInstance<PagingDataAdapter<*, *>>()
            ?.forEach { it.retry() }
    }

    private val loadStateListener: (CombinedLoadStates) -> Unit = { loadState ->
        if (view?.parent != null) {
            recyclerView.isVisible = loadState.source.refresh is LoadState.NotLoading
            resourceLoadingBinding?.apply {
                root.isVisible = loadState.source.refresh !is LoadState.NotLoading
                progressBar.isVisible = loadState.source.refresh is LoadState.Loading
                btnRetry.isVisible = loadState.source.refresh is LoadState.Error
                tvError.isVisible = loadState.source.refresh is LoadState.Error

                if (loadState.refresh is LoadState.NotLoading
                    && loadState.append.endOfPaginationReached
                    && (recyclerView.adapter?.itemCount ?: 0) < 1
                ) {
                    root.isVisible = true
                    tvNoData.isVisible = true
                    recyclerView.isVisible = false
                } else {
                    tvNoData.isVisible = false
                }
            }
        }
    }

    override fun onNavigationItemReselected(item: MenuItem) {
        recyclerView.smoothScrollToPosition(0)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        (recyclerView.adapter as? PagingDataAdapter<*, *>)?.removeLoadStateListener(
            loadStateListener
        )
        recyclerView.adapter = null
    }

}